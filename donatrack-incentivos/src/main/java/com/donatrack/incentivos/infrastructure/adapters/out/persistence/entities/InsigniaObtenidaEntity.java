package com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "insignias_obtenidas", schema = "incentivos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InsigniaObtenidaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "insignia_obtenida_id")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "perfil_donante_id", nullable = false)
  private PerfilDonanteEntity perfil;

  @ManyToOne
  @JoinColumn(name = "insignia_id", nullable = false)
  private InsigniaEntity insignia;

  @Column(name = "fecha_obtencion")
  private LocalDate fechaObtencion;

  @Column(name = "visible_publicamente")
  private Boolean visiblePublicamente;
}
