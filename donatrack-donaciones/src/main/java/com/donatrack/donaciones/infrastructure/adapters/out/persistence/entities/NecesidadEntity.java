package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "necesidades", schema = "donaciones")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class NecesidadEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "descripcion", nullable = false)
  private String descripcion;

  @Column(name = "fecha_solicitud", nullable = false)
  private LocalDate fechaSolicitud;

  @ManyToOne
  @JoinColumn(name = "subcategoria_id", nullable = false)
  private SubcategoriaEntity subcategoriaRequerida;

  @ManyToOne
  @JoinColumn(name = "beneficiario_id", nullable = false)
  private BeneficiarioEntity beneficiario;
}
