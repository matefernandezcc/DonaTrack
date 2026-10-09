package com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.donaciones.domain.entities.enums.MedioContacto;
import com.donatrack.donaciones.domain.entities.enums.TipoDocumento;
import com.donatrack.donaciones.domain.entities.enums.TipoPersonaJuridica;
import com.donatrack.donaciones.domain.entities.persona.Contacto;
import com.donatrack.donaciones.domain.entities.persona.DocumentoIdentidad;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.domain.entities.persona.PersonaHumana;
import com.donatrack.donaciones.domain.entities.persona.PersonaJuridica;
import com.donatrack.donaciones.domain.entities.persona.ubicacion.Direccion;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DireccionEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.PersonaEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.PersonaHumanaEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.PersonaJuridicaEntity;
import java.util.stream.Collectors;

public class PersonaMapper {

  public static PersonaEntity toEntity(Persona domain) {
    if (domain == null) return null;

    PersonaEntity entity;
    if (domain instanceof PersonaHumana ph) {
      PersonaHumanaEntity phEntity = new PersonaHumanaEntity();
      phEntity.setNombre(ph.getNombre());
      phEntity.setApellido(ph.getApellido());
      phEntity.setEdad(ph.getEdad());
      entity = phEntity;
    } else if (domain instanceof PersonaJuridica pj) {
      PersonaJuridicaEntity pjEntity = new PersonaJuridicaEntity();
      pjEntity.setRazonSocial(pj.getRazonSocial());
      pjEntity.setTipo(pj.getTipo() != null ? pj.getTipo().name() : null);
      pjEntity.setRubro(pj.getRubro());
      entity = pjEntity;
    } else {
      throw new IllegalArgumentException("Tipo de persona desconocido");
    }

    entity.setId(domain.getId());
    entity.setEmail(domain.getEmail());

    if (domain.getContacto() != null) {
      entity.setContactoCorreo(domain.getContacto().getCorreoElectronico());
      entity.setContactoTelefono(domain.getContacto().getTelefono());
      entity.setContactoWhatsapp(domain.getContacto().getWhatsapp());
      if (domain.getContacto().getMedioPredeterminado() != null) {
        entity.setContactoMedioPredeterminado(domain.getContacto().getMedioPredeterminado().name());
      }
    }

    if (domain.getDocumento() != null) {
      entity.setDocTipo(
          domain.getDocumento().getTipo() != null ? domain.getDocumento().getTipo().name() : null);
      entity.setDocNumero(domain.getDocumento().getNumero());
    }

    if (domain.getDireccion() != null) {
      DireccionEntity dirEntity = new DireccionEntity();
      dirEntity.setCalle(domain.getDireccion().getCalle());
      dirEntity.setAltura(domain.getDireccion().getAltura());
      dirEntity.setLocalidad(domain.getDireccion().getLocalidad());
      if (domain.getDireccion().getProvincia() != null) {
        dirEntity.setProvincia(domain.getDireccion().getProvincia().getNombreProvincia());
        if (domain.getDireccion().getProvincia().getPais() != null) {
          dirEntity.setPais(domain.getDireccion().getProvincia().getPais().getNombrePais());
        }
      }
      if (domain.getDireccion().getCodigoPostal() != null) {
        dirEntity.setCp(domain.getDireccion().getCodigoPostal());
      }
      if (domain.getDireccion().getCoordenadas() != null) {
        dirEntity.setLatitud(domain.getDireccion().getCoordenadas().getLatitud());
        dirEntity.setLongitud(domain.getDireccion().getCoordenadas().getLongitud());
      }
      entity.setDireccion(dirEntity);
    }

    // Roles Mapping
    if (domain.getRoles() != null) {
      entity.setRoles(
          domain.getRoles().stream()
              .map(
                  rol -> {
                    var rolEntity = RolMapper.toEntity(rol);
                    if (rolEntity != null) {
                      rolEntity.setPersona(entity);
                    }
                    return rolEntity;
                  })
              .filter(java.util.Objects::nonNull)
              .collect(Collectors.toList()));
    }

    return entity;
  }

  public static Persona toDomain(PersonaEntity entity) {
    if (entity == null) return null;

    Contacto contacto =
        new Contacto(
            entity.getContactoCorreo(),
            entity.getContactoTelefono(),
            entity.getContactoWhatsapp(),
            entity.getContactoMedioPredeterminado() != null
                ? MedioContacto.valueOf(entity.getContactoMedioPredeterminado())
                : null);

    Direccion direccion = null;
    if (entity.getDireccion() != null) {
      DireccionEntity de = entity.getDireccion();
      direccion =
          new Direccion(
              de.getCalle(),
              de.getAltura() != null ? de.getAltura() : 0.0,
              de.getLocalidad(),
              null, // provincia
              de.getCp(),
              null // coordenadas
              );
    }

    DocumentoIdentidad documento = null;
    if (entity.getDocTipo() != null || entity.getDocNumero() != null) {
      documento =
          new DocumentoIdentidad(
              entity.getDocTipo() != null ? TipoDocumento.valueOf(entity.getDocTipo()) : null,
              entity.getDocNumero());
    }

    Persona domain;
    if (entity instanceof PersonaHumanaEntity phEntity) {
      domain =
          new PersonaHumana(
              entity.getEmail(),
              contacto,
              direccion,
              documento,
              phEntity.getNombre(),
              phEntity.getApellido(),
              phEntity.getEdad() != null ? phEntity.getEdad() : 0);
    } else if (entity instanceof PersonaJuridicaEntity pjEntity) {
      domain =
          new PersonaJuridica(
              entity.getEmail(),
              contacto,
              direccion,
              documento,
              pjEntity.getRazonSocial(),
              pjEntity.getTipo() != null ? TipoPersonaJuridica.valueOf(pjEntity.getTipo()) : null,
              pjEntity.getRubro());
    } else {
      throw new IllegalArgumentException("Tipo de persona entity desconocido");
    }

    domain.setId(entity.getId());

    if (entity.getRoles() != null) {
      domain.setRoles(
          entity.getRoles().stream()
              .map(RolMapper::toDomain)
              .filter(java.util.Objects::nonNull)
              .collect(Collectors.toList()));
    }

    return domain;
  }
}
