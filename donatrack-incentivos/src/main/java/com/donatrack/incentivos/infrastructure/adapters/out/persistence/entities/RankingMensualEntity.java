package com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rankings_mensuales", schema = "incentivos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RankingMensualEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "ranking_mensual_id")
  private UUID id;

  @Column(name = "mes", length = 20)
  private String mes;

  @Column(name = "anio")
  private Integer anio;

  @OneToMany(mappedBy = "rankingMensual", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PosicionRankingEntity> posiciones = new ArrayList<>();
}
