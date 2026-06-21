package com.donatrack.logistica.domain.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Entrega {
  // Equivale al ID de la donación original para mantener trazabilidad entre módulos
  private UUID idEntrega;
  private EstadoEntrega estado;
  private Double pesoEstimado;
  private Double volumenEstimado;
  private ComprobanteRecepcion comprobanteRecepcion;

  public void marcarEnTraslado() {
    this.estado = EstadoEntrega.EN_TRASLADO;
  }

  public void confirmarRecepcion(List<String> fotos, String camionPatente) {
    this.estado = EstadoEntrega.ENTREGADA;
    this.comprobanteRecepcion = new ComprobanteRecepcion(LocalDateTime.now(), fotos, camionPatente);
  }

  public void confirmarRecepcion(List<String> fotos) {
    this.confirmarRecepcion(fotos, null);
  }

  public void marcarNoRecibida() {
    this.estado = EstadoEntrega.NO_RECIBIDA;
  }

  public void volverAPendiente() {
    this.estado = EstadoEntrega.PENDIENTE;
  }
}
