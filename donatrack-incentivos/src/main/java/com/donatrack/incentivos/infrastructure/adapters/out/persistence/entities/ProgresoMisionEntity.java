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
@Table(name = "progreso_misiones", schema = "incentivos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProgresoMisionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "progreso_mision_id")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "perfil_donante_id", nullable = false)
  private PerfilDonanteEntity perfil;

  @ManyToOne
  @JoinColumn(name = "mision_id", nullable = false)
  private MisionEntity mision;

  @Column(name = "progreso_actual")
  private Integer progresoActual;

  @Column(name = "estado", length = 50)
  private String estado;

  @Column(name = "mes_completada", length = 20)
  private String mesCompletada;
}
