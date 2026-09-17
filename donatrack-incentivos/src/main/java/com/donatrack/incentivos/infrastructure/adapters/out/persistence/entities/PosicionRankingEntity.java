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
@Table(name = "posiciones_ranking", schema = "incentivos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PosicionRankingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "posicion_ranking_id")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "ranking_mensual_id", nullable = false)
  private RankingMensualEntity rankingMensual;

  @ManyToOne
  @JoinColumn(name = "perfil_donante_id", nullable = false)
  private PerfilDonanteEntity perfil;

  @Column(name = "posicion")
  private Integer posicion;

  @Column(name = "misiones_completadas_mes")
  private Integer misionesCompletadasMes;
}
