package com.donatrack.donaciones.domain.entities.donacion;

import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import com.donatrack.donaciones.domain.entities.roles.Administrador;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Donacion {
  private UUID id;
  private LocalDate fechaCreacion;
  private EstadoDonacion estado;
  private List<Bien> bienes;
  private Subcategoria subCategoria;
  private List<HistorialEstado> historial;
  private LocalDate fechaVencimiento;
  private Beneficiario entidadAsignada;
  private List<Foto> fotos;

  public Donacion(Subcategoria subcategoria) {
    this.id = UUID.randomUUID();
    this.fechaCreacion = LocalDate.now();
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

  public List<String> getCategoriasString() {
    List<String> categorias = new ArrayList<>();
    for (Bien bien : bienes) {
      categorias.add(bien.getSubcategoria().getNombre());
    }
    return categorias;
  }
}
