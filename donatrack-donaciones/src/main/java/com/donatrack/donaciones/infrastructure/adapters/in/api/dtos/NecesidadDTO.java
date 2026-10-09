package com.donatrack.donaciones.infrastructure.adapters.in.api.dtos;

import com.donatrack.donaciones.domain.entities.donacion.Subcategoria;
import com.donatrack.donaciones.domain.entities.enums.EstadoNecesidad;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tipo")
@JsonSubTypes({
    @JsonSubTypes.Type(value = NecesidadExtraordinariaDTO.class, name = "extraordinaria"),
    @JsonSubTypes.Type(value = NecesidadRecurrenteDTO.class, name = "recurrente")
})
@Getter
@Setter
public abstract class NecesidadDTO {
  private UUID id;
  private String descripcion;
  private LocalDate fechaSolicitud;
  private Subcategoria subcategoriaRequerida;
  private EstadoNecesidad estado;
}
