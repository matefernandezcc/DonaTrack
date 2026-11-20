package com.donatrack.donaciones.application.ports.out;

import com.donatrack.donaciones.domain.entities.donacion.DonacionOriginal;
import java.util.Optional;
import java.util.UUID;

public interface DonacionOriginalRepository {
  void guardar(DonacionOriginal donacionOriginal);

  Optional<DonacionOriginal> buscarPorId(UUID id);

  Optional<DonacionOriginal> buscarPorIdDonacion(UUID idDonacion);

  default java.util.Map<UUID, String[]> obtenerMapaDonantesPorDonacion() {
    return java.util.Collections.emptyMap();
  }
}
