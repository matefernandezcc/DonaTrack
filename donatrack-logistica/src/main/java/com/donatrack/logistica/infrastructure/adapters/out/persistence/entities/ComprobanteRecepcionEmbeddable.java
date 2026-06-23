package com.donatrack.logistica.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteRecepcionEmbeddable {

  @Column(name = "comprobante_fecha_hora")
  private LocalDateTime comprobanteFechaHora;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "comprobante_fotos", columnDefinition = "text[]")
  private List<String> comprobanteFotos;

  @Column(name = "comprobante_camion_patente", length = 10)
  private String comprobanteCamionPatente;
}
