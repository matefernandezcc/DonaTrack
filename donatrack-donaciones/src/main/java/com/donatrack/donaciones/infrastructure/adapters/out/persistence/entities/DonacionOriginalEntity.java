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
@Table(name = "donaciones_originales", schema = "donaciones")
@Getter
@Setter
public class DonacionOriginalEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "donacion_original_id")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "donante_id")
  private DonanteEntity donante;

  @Column(name = "descripcion_general", columnDefinition = "TEXT")
  private String descripcionGeneral;

  @Column(name = "fecha_recepcion")
  private LocalDateTime fechaRecepcion;

  @Column(name = "usuario_id")
  private String usuarioId;

  @OneToMany(mappedBy = "donacionOriginal", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DonacionEntity> donaciones = new ArrayList<>();
}
