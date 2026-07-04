package com.donatrack.donaciones.application.ports.in;

import com.donatrack.donaciones.domain.entities.donacion.DonacionOriginal;

public interface RecepcionDonacionesUseCase {
    DonacionOriginal recibir(CargaBienesRequestDTO requestDTO);
}
