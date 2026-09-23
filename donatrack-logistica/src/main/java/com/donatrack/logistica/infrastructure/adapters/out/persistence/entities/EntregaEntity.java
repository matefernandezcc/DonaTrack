package com.donatrack.logistica.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "entregas", schema = "logistica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EntregaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "entrega_id")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "parada_id", nullable = false)
  private ParadaEntity parada;

  @Column(name = "id_donacion")
  private UUID idDonacion;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado", length = 50, nullable = false)
  private EstadoEntregaEnum estado;

  @Column(name = "peso_estimado")
  private Double pesoEstimado;

  @Column(name = "volumen_estimado")
  private Double volumenEstimado;

  @Embedded private ComprobanteRecepcionEmbeddable comprobanteRecepcion;

  @Column(name = "justificacion_fallo", columnDefinition = "TEXT")
  private String justificacionFallo;

  public enum EstadoEntregaEnum {
    PENDIENTE,
    EN_TRASLADO,
    ENTREGADA,
    NO_RECIBIDA
  }
}
