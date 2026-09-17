package com.donatrack.logistica.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "paradas", schema = "logistica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParadaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "parada_id")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "ruta_reparto_id", nullable = false)
  private RutaDeRepartoEntity ruta;

  @Column(name = "orden", nullable = false)
  private Integer orden;

  @Column(name = "calle", length = 255)
  private String calle;

  @Column(name = "altura", length = 255)
  private String altura;

  @Column(name = "localidad", length = 255)
  private String localidad;

  @Column(name = "latitud")
  private Double latitud;

  @Column(name = "longitud")
  private Double longitud;

  @OneToMany(mappedBy = "parada", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<EntregaEntity> entregas = new ArrayList<>();
}
