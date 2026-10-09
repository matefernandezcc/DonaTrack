package com.donatrack.donaciones.infrastructure.adapters.in.api.dtos;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Schema(
    description =
        "Persona registrada en el sistema. Campo 'tipo' obligatorio: 'HUMANA' o 'JURIDICA'")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tipo")
@JsonSubTypes({
  @JsonSubTypes.Type(value = PersonaHumanaDTO.class, name = "HUMANA"),
  @JsonSubTypes.Type(value = PersonaJuridicaDTO.class, name = "JURIDICA")
})
@Getter
@Setter
public abstract class PersonaDTO {
  private UUID id;
  private String email;
  private ContactoDTO contacto;
  private DireccionDTO direccion;
  private DocumentoIdentidadDTO documento;
}
