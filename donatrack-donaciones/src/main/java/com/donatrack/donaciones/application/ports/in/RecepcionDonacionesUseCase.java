package com.donatrack.donaciones.application.ports.in;

import com.donatrack.donaciones.domain.entities.donacion.RecepcionDonacion;

public interface RecepcionDonacionesUseCase {
    RecepcionDonacion recibir(CargaBienesRequestDTO requestDTO);
}
