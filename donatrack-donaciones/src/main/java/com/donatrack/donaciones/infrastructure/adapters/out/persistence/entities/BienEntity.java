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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bienes", schema = "donaciones")
@Getter
@Setter
public class BienEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id")
  private UUID id;

  @Column(name = "descripcion", nullable = false)
  private String descripcion;

  @Column(name = "cantidad")
  private Double cantidad;

  @Column(name = "unidad_medicion")
  private String unidadMedicion;

  @Column(name = "es_usado")
  private Boolean esUsado;

  @Column(name = "fecha_vencimiento")
  private LocalDate fechaVencimiento;

  @ManyToOne
  @JoinColumn(name = "subcategoria_id")
  private SubcategoriaEntity subcategoria;

  @OneToMany(mappedBy = "bien", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FotoEntity> fotos;

  @ManyToOne
  @JoinColumn(name = "donacion_id")
  private DonacionEntity donacion;
}
