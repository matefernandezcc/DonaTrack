package com.donatrack.notificaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "eventos_notificacion", schema = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventoNotificacionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "evento_notificacion_id")
  private UUID id;

  @Column(name = "tipo_evento", length = 50)
  private String tipoEvento;

  @Column(name = "descripcion", length = 255)
  private String descripcion;

  @Column(name = "fecha_recepcion")
  private LocalDateTime fechaRecepcion;

  @OneToMany(mappedBy = "eventoNotificacion", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<NotificacionEntity> notificaciones = new ArrayList<>();
}
