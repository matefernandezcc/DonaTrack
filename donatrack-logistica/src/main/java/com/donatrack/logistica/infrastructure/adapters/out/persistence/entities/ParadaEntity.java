package com.donatrack.logistica.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "paradas", schema = "logistica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParadaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "ruta_id", nullable = false)
  private RutaDeRepartoEntity ruta;

  @Column(name = "orden", nullable = false)
  private Integer orden;

  @Embedded private DireccionEmbeddable direccion;

  @Embedded private CoordenadaEmbeddable coordenada;

  @OneToMany(mappedBy = "parada", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<EntregaEntity> entregas;
}
