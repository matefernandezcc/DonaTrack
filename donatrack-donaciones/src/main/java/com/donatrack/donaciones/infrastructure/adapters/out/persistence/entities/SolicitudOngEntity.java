package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "solicitudes_ong", schema = "donaciones")
@Getter
@Setter
public class SolicitudOngEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "solicitud_id")
  private UUID id;

  @Column(name = "nombre_ong", nullable = false)
  private String nombreOng;

  @Column(name = "cuit")
  private String cuit;

  @Column(name = "direccion")
  private String direccion;

  @Column(name = "email")
  private String email;

  @Column(name = "telefono")
  private String telefono;

  @Column(name = "responsable")
  private String responsable;

  @Column(name = "tipo_entidad")
  private String tipoEntidad;

  @Column(name = "necesidades", length = 2000)
  private String necesidades;

  @Column(name = "estado", nullable = false)
  private String estado = "PENDIENTE";

  @Column(name = "fecha_creacion")
  private LocalDateTime fechaCreacion;

  public SolicitudOngEntity() {
    this.fechaCreacion = LocalDateTime.now();
  }

  public SolicitudOngEntity(
      String nombreOng,
      String cuit,
      String direccion,
      String email,
      String telefono,
      String responsable,
      String tipoEntidad,
      String necesidades,
      String estado) {
    this.nombreOng = nombreOng;
    this.cuit = cuit;
    this.direccion = direccion;
    this.email = email;
    this.telefono = telefono;
    this.responsable = responsable;
    this.tipoEntidad = tipoEntidad;
    this.necesidades = necesidades;
    this.estado = estado != null ? estado : "PENDIENTE";
    this.fechaCreacion = LocalDateTime.now();
  }
}
