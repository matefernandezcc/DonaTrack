package com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "misiones", schema = "incentivos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MisionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "mision_id")
  private UUID id;

  @Column(name = "nombre", nullable = false)
  private String nombre;

  @ManyToOne
  @JoinColumn(name = "recompensa_insignia_id")
  private InsigniaEntity recompensaInsignia;

  @Column(name = "tipo_metrica")
  private String tipoMetrica;

  @Column(name = "objetivo")
  private Integer objetivo;

  @Column(name = "orden_ejecucion")
  private Integer ordenEjecucion;
}
