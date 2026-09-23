package com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "metricas_donante", schema = "incentivos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetricasDonanteEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "metricas_donante_id")
  private UUID id;

  @OneToOne
  @JoinColumn(name = "perfil_donante_id", unique = true, nullable = false)
  private PerfilDonanteEntity perfil;

  @Column(name = "total_donaciones_historicas")
  private Integer totalDonacionesHistoricas;

  @Column(name = "organizaciones_ayudadas")
  private Integer organizacionesAyudadas;

  @OneToMany(mappedBy = "metricas", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RegistroDonacionEntity> registrosDonacion = new ArrayList<>();
}
