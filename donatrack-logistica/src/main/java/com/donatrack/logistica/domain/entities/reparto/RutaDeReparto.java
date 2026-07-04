package com.donatrack.logistica.domain.entities.reparto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.donatrack.logistica.domain.entities.entregas.Entrega;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RutaDeReparto {
  private UUID id;
  private LocalDate fechaOperativa;
  private Boolean iniciada;
  private Camion camion;
  private Chofer chofer;
  private List<Parada> paradas;

  public void iniciarRuta() {
    this.iniciada = true;
    if (this.paradas != null) {
      for (Parada parada : this.paradas) {
        if (parada.getEntregas() != null) {
          for (Entrega entrega : parada.getEntregas()) {
            entrega.marcarEnTraslado();
          }
        }
      }
    }
  }
}
