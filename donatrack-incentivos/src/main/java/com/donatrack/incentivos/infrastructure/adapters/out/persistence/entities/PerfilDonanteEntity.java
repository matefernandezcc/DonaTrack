package com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "perfiles_donante", schema = "incentivos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerfilDonanteEntity {

  @Id
  @Column(name = "perfil_donante_id")
  private UUID perfilDonanteId;

  @Column(name = "categoria", length = 50)
  private String categoria;

  @ManyToOne
  @JoinColumn(name = "mision_actual_id")
  private MisionEntity misionActual;

  @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<InsigniaObtenidaEntity> insigniasObtenidas = new ArrayList<>();

  @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProgresoMisionEntity> progresosMisiones = new ArrayList<>();

  @OneToOne(mappedBy = "perfil", cascade = CascadeType.ALL, orphanRemoval = true)
  private MetricasDonanteEntity metricas;

  @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PosicionRankingEntity> posicionesRanking = new ArrayList<>();
}
