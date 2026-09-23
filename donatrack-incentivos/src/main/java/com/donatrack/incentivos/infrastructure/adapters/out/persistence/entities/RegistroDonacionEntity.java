package com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "registros_donacion", schema = "incentivos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroDonacionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "registro_donacion_id")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "metricas_donante_id", nullable = false)
  private MetricasDonanteEntity metricas;

  @Column(name = "id_donacion_origen")
  private UUID idDonacionOrigen;

  @Column(name = "cantidad_bienes")
  private Integer cantidadBienes;

  @Column(name = "categorias", columnDefinition = "TEXT")
  private String categorias;

  @Column(name = "id_entidad_beneficiaria_origen")
  private UUID idEntidadBeneficiariaOrigen;

  @Column(name = "mes_donacion", length = 20)
  private String mesDonacion;
}
