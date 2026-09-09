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
@Table(name = "donaciones_originales", schema = "donaciones")
@Getter
@Setter
public class DonacionOriginalEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "fecha_recepcion", nullable = false)
  private LocalDateTime fechaRecepcion;

  @ManyToOne
  @JoinColumn(name = "donante_id", nullable = false)
  private DonanteEntity donante;

  @OneToMany(mappedBy = "donacionOriginal", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DonacionEntity> donaciones;
}
