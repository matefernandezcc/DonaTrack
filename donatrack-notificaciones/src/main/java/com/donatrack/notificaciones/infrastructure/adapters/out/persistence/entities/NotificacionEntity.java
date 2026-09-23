package com.donatrack.notificaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notificaciones", schema = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "notificacion_id")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "evento_notificacion_id")
  private EventoNotificacionEntity eventoNotificacion;

  @Column(name = "destinatario", length = 255)
  private String destinatario;

  @Column(name = "medio", length = 50)
  private String medio;

  @Column(name = "mensaje", columnDefinition = "TEXT")
  private String mensaje;

  @Column(name = "fecha_envio")
  private LocalDateTime fechaEnvio;

  @Column(name = "completada")
  private Boolean completada = false;
}
