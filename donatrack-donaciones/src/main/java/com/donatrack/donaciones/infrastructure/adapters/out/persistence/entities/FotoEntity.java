package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "fotos", schema = "donaciones")
@Getter
@Setter
public class FotoEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "foto_id")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "bien_id")
  private BienEntity bien;

  @Column(name = "descripcion")
  private String descripcion;

  @Column(name = "url", length = 500)
  private String url;
}
