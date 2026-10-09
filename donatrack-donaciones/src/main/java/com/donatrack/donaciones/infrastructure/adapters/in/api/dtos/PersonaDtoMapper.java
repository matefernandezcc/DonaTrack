package com.donatrack.donaciones.infrastructure.adapters.in.api.dtos;

import com.donatrack.donaciones.domain.entities.enums.MedioContacto;
import com.donatrack.donaciones.domain.entities.enums.TipoDocumento;
import com.donatrack.donaciones.domain.entities.enums.TipoPersonaJuridica;
import com.donatrack.donaciones.domain.entities.persona.Contacto;
import com.donatrack.donaciones.domain.entities.persona.DocumentoIdentidad;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.domain.entities.persona.PersonaHumana;
import com.donatrack.donaciones.domain.entities.persona.PersonaJuridica;
import com.donatrack.donaciones.domain.entities.persona.ubicacion.Coordenada;
import com.donatrack.donaciones.domain.entities.persona.ubicacion.Direccion;
import com.donatrack.donaciones.domain.entities.persona.ubicacion.Pais;
import com.donatrack.donaciones.domain.entities.persona.ubicacion.Provincia;

public class PersonaDtoMapper {

  // ── DTO → Dominio ──

  public static Persona toDomain(PersonaDTO dto) {
    if (dto == null) return null;

    Contacto contacto = toContactoDomain(dto.getContacto());
    Direccion direccion = toDireccionDomain(dto.getDireccion());
    DocumentoIdentidad documento = toDocumentoDomain(dto.getDocumento());

    Persona persona;
    if (dto instanceof PersonaHumanaDTO h) {
      persona = new PersonaHumana(
          dto.getEmail(), contacto, direccion, documento,
          h.getNombre(), h.getApellido(), h.getEdad());
    } else if (dto instanceof PersonaJuridicaDTO j) {
      TipoPersonaJuridica tipoJur = j.getTipo() != null
          ? TipoPersonaJuridica.valueOf(j.getTipo()) : null;
      persona = new PersonaJuridica(
          dto.getEmail(), contacto, direccion, documento,
          j.getRazonSocial(), tipoJur, j.getRubro());
    } else {
      throw new IllegalArgumentException("PersonaDTO no soportado");
    }

    if (dto.getId() != null) persona.setId(dto.getId());
    return persona;
  }

  // ── Dominio → DTO ──

  public static PersonaDTO toDto(Persona persona) {
    if (persona == null) return null;

    PersonaDTO dto;
    if (persona instanceof PersonaHumana h) {
      PersonaHumanaDTO hdto = new PersonaHumanaDTO();
      hdto.setNombre(h.getNombre());
      hdto.setApellido(h.getApellido());
      hdto.setEdad(h.getEdad());
      dto = hdto;
    } else if (persona instanceof PersonaJuridica j) {
      PersonaJuridicaDTO jdto = new PersonaJuridicaDTO();
      jdto.setRazonSocial(j.getRazonSocial());
      jdto.setTipo(j.getTipo() != null ? j.getTipo().name() : null);
      jdto.setRubro(j.getRubro());
      dto = jdto;
    } else {
      throw new IllegalArgumentException("Persona no soportada");
    }

    dto.setId(persona.getId());
    dto.setEmail(persona.getEmail());
    dto.setContacto(toContactoDto(persona.getContacto()));
    dto.setDireccion(toDireccionDto(persona.getDireccion()));
    dto.setDocumento(toDocumentoDto(persona.getDocumento()));
    return dto;
  }

  // ── Helpers Contacto ──

  private static Contacto toContactoDomain(ContactoDTO dto) {
    if (dto == null) return null;
    MedioContacto medio = dto.getMedioPredeterminado() != null
        ? MedioContacto.valueOf(dto.getMedioPredeterminado()) : null;
    return new Contacto(dto.getCorreoElectronico(), dto.getTelefono(), dto.getWhatsapp(), medio);
  }

  private static ContactoDTO toContactoDto(Contacto c) {
    if (c == null) return null;
    ContactoDTO dto = new ContactoDTO();
    dto.setCorreoElectronico(c.getCorreoElectronico());
    dto.setTelefono(c.getTelefono());
    dto.setWhatsapp(c.getWhatsapp());
    dto.setMedioPredeterminado(c.getMedioPredeterminado() != null
        ? c.getMedioPredeterminado().name() : null);
    return dto;
  }

  // ── Helpers Documento ──

  private static DocumentoIdentidad toDocumentoDomain(DocumentoIdentidadDTO dto) {
    if (dto == null) return null;
    TipoDocumento tipo = dto.getTipo() != null ? TipoDocumento.valueOf(dto.getTipo()) : null;
    return new DocumentoIdentidad(tipo, dto.getNumero());
  }

  private static DocumentoIdentidadDTO toDocumentoDto(DocumentoIdentidad d) {
    if (d == null) return null;
    DocumentoIdentidadDTO dto = new DocumentoIdentidadDTO();
    dto.setTipo(d.getTipo() != null ? d.getTipo().name() : null);
    dto.setNumero(d.getNumero());
    return dto;
  }

  // ── Helpers Direccion ──

  private static Direccion toDireccionDomain(DireccionDTO dto) {
    if (dto == null) return null;
    Provincia prov = null;
    if (dto.getProvincia() != null) {
      Pais pais = null;
      if (dto.getProvincia().getPais() != null) {
        pais = new Pais(dto.getProvincia().getPais().getNombre(), null);
      }
      prov = new Provincia(dto.getProvincia().getNombreProvincia(), pais);
    }
    Coordenada coord = null;
    if (dto.getCoordenadas() != null) {
      coord = new Coordenada(dto.getCoordenadas().getLatitud(), dto.getCoordenadas().getLongitud());
    }
    return new Direccion(dto.getCalle(),
        dto.getAltura() != null ? dto.getAltura() : 0.0,
        dto.getLocalidad(), prov, dto.getCodigoPostal(), coord);
  }

  private static DireccionDTO toDireccionDto(Direccion d) {
    if (d == null) return null;
    DireccionDTO dto = new DireccionDTO();
    dto.setCalle(d.getCalle());
    dto.setAltura(d.getAltura());
    dto.setLocalidad(d.getLocalidad());
    dto.setCodigoPostal(d.getCodigoPostal());
    if (d.getProvincia() != null) {
      ProvinciaDTO pdto = new ProvinciaDTO();
      pdto.setNombreProvincia(d.getProvincia().getNombreProvincia());
      if (d.getProvincia().getPais() != null) {
        PaisDTO paisdto = new PaisDTO();
        paisdto.setNombre(d.getProvincia().getPais().getNombrePais());
        pdto.setPais(paisdto);
      }
      dto.setProvincia(pdto);
    }
    if (d.getCoordenadas() != null) {
      CoordenadaDTO cdto = new CoordenadaDTO();
      cdto.setLatitud(d.getCoordenadas().getLatitud());
      cdto.setLongitud(d.getCoordenadas().getLongitud());
      dto.setCoordenadas(cdto);
    }
    return dto;
  }
}
