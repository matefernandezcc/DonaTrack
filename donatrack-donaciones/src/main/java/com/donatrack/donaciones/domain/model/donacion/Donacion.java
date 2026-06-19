package com.donatrack.donaciones.domain.model.donacion;

import com.donatrack.donaciones.domain.model.donacion.estado.EnDeposito;
import com.donatrack.donaciones.domain.model.donacion.estado.EstadoDonacion;
import com.donatrack.donaciones.domain.model.donacion.estado.HistorialEstado;
import com.donatrack.donaciones.domain.model.roles.Administrador;
import com.donatrack.donaciones.domain.model.roles.Beneficiario;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

public class Donacion {
  @Getter @Setter private UUID id;
  @Getter private EstadoDonacion estado;
  @Getter @Setter private List<Bien> bienes;
  @Getter @Setter private Subcategoria subCategoria;
  @Getter @Setter private List<HistorialEstado> historial;
  @Getter @Setter private LocalDate fechaVencimiento;
  @Getter @Setter private Beneficiario entidadAsignada;
  @Getter private List<Foto> fotos;

  public Donacion(Subcategoria subcategoria) {
    this.id = UUID.randomUUID();
    this.estado = new EnDeposito(this);
    this.bienes = new ArrayList<>();
    this.subCategoria = subcategoria;
    this.historial = new ArrayList<>();
  }

  public void agregarBien(Bien b) {
    this.bienes.add(b);
  }

  public void registrarCambioEstado(HistorialEstado nuevoRegistro) {
    this.historial.add(nuevoRegistro);
  }

  public void cambiarEstado(EstadoDonacion nuevoEstado, String observacion, Administrador usuario) {
    this.estado = nuevoEstado;
    this.historial.add(new HistorialEstado(nuevoEstado.getValorEnum(), observacion, usuario));
  }

  public void asignar(Beneficiario beneficiario) {
    this.estado.asignar(beneficiario);
  }

  public void planificarRuta() {
    this.estado.planificarRuta();
  }

  public void iniciarTraslado() {
    this.estado.iniciarTraslado();
  }

  public void entregar() {
    this.estado.entregar();
  }

  public void fallarEntrega(String justificacion) {
    this.estado.fallarEntrega(justificacion);
  }

  public void marcarVencida() {
    this.estado.marcarVencida();
  }

  public void recibirEnDeposito() {
    this.estado.recibirEnDeposito();
  }

  public void addFoto(Foto foto) {
    this.fotos.add(foto);
  }
}
