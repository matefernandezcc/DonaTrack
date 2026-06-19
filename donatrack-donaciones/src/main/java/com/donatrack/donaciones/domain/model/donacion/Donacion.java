package com.donatrack.donaciones.domain.model.donacion;

import com.donatrack.donaciones.domain.model.enums.EstadoDonacion;
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
    this.estado = EstadoDonacion.EN_DEPOSITO;
    this.bienes = new ArrayList<>();
    this.subCategoria = subcategoria;
    this.historial = new ArrayList<>();
    this.fotos = new ArrayList<>();
  }

  public void agregarBien(Bien b) {
    this.bienes.add(b);
  }

  public void registrarCambioEstado(HistorialEstado nuevoRegistro) {
    this.historial.add(nuevoRegistro);
  }

  public void cambiarEstado(EstadoDonacion nuevoEstado, String observacion, Administrador usuario) {
    this.estado = nuevoEstado;
    this.historial.add(new HistorialEstado(nuevoEstado, observacion, usuario));
  }

  public void asignar(Beneficiario beneficiario) {
    this.entidadAsignada = beneficiario;
    this.cambiarEstado(EstadoDonacion.ASIGNADA, "Donación asignada", null);
  }

  public void addFoto(Foto foto) {
    this.fotos.add(foto);
  }
}
