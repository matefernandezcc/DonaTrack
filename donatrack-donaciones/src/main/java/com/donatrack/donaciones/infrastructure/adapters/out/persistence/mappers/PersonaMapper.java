package com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.donaciones.domain.entities.persona.Contacto;
import com.donatrack.donaciones.domain.entities.persona.DocumentoIdentidad;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.domain.entities.persona.PersonaHumana;
import com.donatrack.donaciones.domain.entities.persona.PersonaJuridica;
import com.donatrack.donaciones.domain.entities.persona.ubicacion.Direccion;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.ContactoEmbeddable;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DireccionEmbeddable;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DocumentoIdentidadEmbeddable;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.PersonaEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.PersonaHumanaEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.PersonaJuridicaEntity;
import com.donatrack.donaciones.domain.entities.enums.TipoPersonaJuridica;
import java.util.stream.Collectors;

public class PersonaMapper {

  public static PersonaEntity toEntity(Persona domain) {
    if (domain == null) return null;

    PersonaEntity entity;
    if (domain instanceof PersonaHumana ph) {
      PersonaHumanaEntity phEntity = new PersonaHumanaEntity();
      phEntity.setNombre(ph.getNombre());
      phEntity.setApellido(ph.getApellido());
      entity = phEntity;
    } else if (domain instanceof PersonaJuridica pj) {
      PersonaJuridicaEntity pjEntity = new PersonaJuridicaEntity();
      pjEntity.setRazonSocial(pj.getRazonSocial());
      pjEntity.setTipo(pj.getTipo().name());
      pjEntity.setCuit(pj.getDocumento().getNumero());
      entity = pjEntity;
    } else {
      throw new IllegalArgumentException("Tipo de persona desconocido");
    }

    entity.setId(domain.getId());
    entity.setContacto(mapContacto(domain.getContacto()));
    entity.setDireccion(mapDireccion(domain.getDireccion()));
    entity.setDocumento(mapDocumento(domain.getDocumento()));

    // Roles Mapping
    if (domain.getRoles() != null) {
      entity.setRoles(
          domain.getRoles().stream()
              .map(rol -> {
                  var rolEntity = RolMapper.toEntity(rol);
                  rolEntity.setPersona(entity);
                  return rolEntity;
              })
              .collect(Collectors.toList()));
    }

    return entity;
  }

  public static Persona toDomain(PersonaEntity entity) {
    if (entity == null) return null;

    Persona domain;
    Contacto contacto = mapContactoDomain(entity.getContacto());
    Direccion direccion = mapDireccionDomain(entity.getDireccion());
    DocumentoIdentidad documento = mapDocumentoDomain(entity.getDocumento());

    if (entity instanceof PersonaHumanaEntity phEntity) {
      domain = new PersonaHumana(
          null, // email
          contacto,
          direccion,
          documento,
          phEntity.getNombre(),
          phEntity.getApellido(),
          0 // edad
      );
    } else if (entity instanceof PersonaJuridicaEntity pjEntity) {
      domain = new PersonaJuridica(
          null, // email
          contacto,
          direccion,
          documento,
          pjEntity.getRazonSocial(),
          TipoPersonaJuridica.valueOf(pjEntity.getTipo()),
          null // rubro
      );
    } else {
      throw new IllegalArgumentException("Tipo de persona entity desconocido");
    }

    domain.setId(entity.getId());
    
    if (entity.getRoles() != null) {
        domain.setRoles(entity.getRoles().stream()
            .map(RolMapper::toDomain)
            .collect(Collectors.toList()));
    }

    return domain;
  }

  // --- Helpers for Embeddables ---

  private static ContactoEmbeddable mapContacto(Contacto c) {
    if (c == null) return null;
    return new ContactoEmbeddable(
        c.getMedioPredeterminado() != null ? c.getMedioPredeterminado().name() : null,
        c.getCorreoElectronico(),
        c.getTelefono(),
        c.getWhatsapp()
    );
  }

  private static Contacto mapContactoDomain(ContactoEmbeddable ce) {
    if (ce == null) return null;
    return new Contacto(
        ce.getCorreo(),
        ce.getTelefono(),
        ce.getWhatsapp(),
        ce.getMedio() != null ? com.donatrack.donaciones.domain.entities.enums.MedioContacto.valueOf(ce.getMedio()) : null
    );
  }

  private static DireccionEmbeddable mapDireccion(Direccion d) {
    if (d == null) return null;
    return new DireccionEmbeddable(
        d.getCalle(),
        String.valueOf(d.getAltura()),
        d.getLocalidad(),
        "Argentina", // Default
        d.getProvincia() != null ? d.getProvincia().getNombreProvincia() : null
    );
  }

  private static Direccion mapDireccionDomain(DireccionEmbeddable de) {
    if (de == null) return null;
    return new Direccion(
        de.getCalle(),
        de.getAltura() != null ? Double.parseDouble(de.getAltura()) : 0.0,
        de.getLocalidad(),
        null, // provincia
        null, // codigo postal
        null  // coordenadas
    );
  }

  private static DocumentoIdentidadEmbeddable mapDocumento(DocumentoIdentidad d) {
    if (d == null) return null;
    return new DocumentoIdentidadEmbeddable(String.valueOf(d.getTipo()), d.getNumero());
  }

  private static DocumentoIdentidad mapDocumentoDomain(DocumentoIdentidadEmbeddable de) {
    if (de == null) return null;
    return new DocumentoIdentidad(
        de.getTipo() != null ? com.donatrack.donaciones.domain.entities.enums.TipoDocumento.valueOf(de.getTipo()) : null,
        de.getNumero()
    );
  }
}
