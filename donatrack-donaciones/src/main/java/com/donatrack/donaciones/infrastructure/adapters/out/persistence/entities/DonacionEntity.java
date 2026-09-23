package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "donaciones", schema = "donaciones")
@Getter
@Setter
public class DonacionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "donacion_id")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "donacion_original_id")
  private DonacionOriginalEntity donacionOriginal;

  @ManyToOne
  @JoinColumn(name = "subcategoria_id")
  private SubcategoriaEntity subcategoriaAsignada;

  @ManyToOne
  @JoinColumn(name = "necesidad_id")
  private NecesidadEntity necesidad;

  @ManyToOne
  @JoinColumn(name = "periodo_id")
  private PeriodoNecesidadEntity periodo;

  @Column(name = "estado")
  private String estado;

  @Column(name = "fecha_creacion")
  private LocalDateTime fechaCreacion;

  @OneToMany(mappedBy = "donacion", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<BienEntity> bienes = new ArrayList<>();

  @OneToMany(mappedBy = "donacion", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<HistorialEstadoEntity> historialEstados = new ArrayList<>();
}
