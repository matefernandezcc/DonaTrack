package com.donatrack.donaciones.domain.model.donacion;

import com.donatrack.donaciones.domain.enums.EstadoDonacion;
import com.donatrack.donaciones.domain.model.roles.Beneficiario;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

public class Donacion {
  @Getter @Setter private UUID id;
  @Getter @Setter private EstadoDonacion estado;
  @Getter @Setter private List<Bien> bienes;
  @Getter @Setter private Categoria subCategoria;
  @Getter @Setter private List<HistorialEstado> historial;
  @Getter @Setter private LocalDate fechaVencimiento;
  @Getter @Setter private Beneficiario entidadAsignada;
  @Getter private List<Foto> fotos;

  public Donacion(Categoria categoria) {
    this.id = UUID.randomUUID();
    this.estado = EstadoDonacion.PENDIENTE;
    this.bienes = new ArrayList<>();
    this.subCategoria = categoria;
    this.historial = new ArrayList<>();
  }

  public void agregarBien(Bien b) {
    this.bienes.add(b);
  }

  public void registrarCambioEstado(HistorialEstado nuevoRegistro) {
    this.historial.add(nuevoRegistro);
    // Cuando agregamos un historial nuevo, actualizamos el estado actual de la donación
    this.estado = nuevoRegistro.getEstado();
  }

  public void addFoto(Foto foto) {
    this.fotos.add(foto);
  }
}
