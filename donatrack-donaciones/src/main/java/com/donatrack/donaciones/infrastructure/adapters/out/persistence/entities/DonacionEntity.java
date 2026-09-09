package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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
  @Column(name = "id")
  private UUID id;

  @Column(name = "estado", nullable = false)
  private String estado;

  @Column(name = "fecha_asignacion")
  private LocalDateTime fechaAsignacion;

  @Column(name = "observaciones")
  private String observaciones;

  @ManyToOne
  @JoinColumn(name = "subcategoria_id")
  private SubcategoriaEntity subcategoriaAsignada;

  @ManyToOne
  @JoinColumn(name = "donacion_original_id")
  private DonacionOriginalEntity donacionOriginal;

  @ManyToOne
  @JoinColumn(name = "beneficiario_id")
  private BeneficiarioEntity beneficiario;

  @OneToMany(mappedBy = "donacion", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<BienEntity> bienes;

  @OneToMany(mappedBy = "donacion", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<HistorialEstadoEntity> historialEstados;

  @OneToMany(mappedBy = "donacion", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FotoEntity> fotosAdicionales;
}
