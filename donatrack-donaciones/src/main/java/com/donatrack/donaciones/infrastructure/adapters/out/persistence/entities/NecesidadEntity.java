package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
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
@Table(name = "necesidades", schema = "donaciones")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class NecesidadEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "necesidad_id")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "beneficiario_id")
  private BeneficiarioEntity beneficiario;

  @ManyToOne
  @JoinColumn(name = "subcategoria_id")
  private SubcategoriaEntity subcategoriaRequerida;

  @Column(name = "descripcion", columnDefinition = "TEXT")
  private String descripcion;

  @Column(name = "fecha_solicitud")
  private LocalDateTime fechaSolicitud;

  @Column(name = "estado")
  private String estado;

  @Column(name = "cantidad_requerida")
  private Double cantidadRequerida;

  @Column(name = "activa")
  private Boolean activa;

  @Column(name = "cantidad_objetivo")
  private Double cantidadObjetivo;

  @Column(name = "tipo_periodo")
  private String tipoPeriodo;

  @OneToMany(mappedBy = "necesidad", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PeriodoNecesidadEntity> periodos = new ArrayList<>();
}
