package com.donatrack.donaciones.application.port.in;

import com.donatrack.donaciones.domain.entities.donacion.RecepcionDonacion;

public interface RecepcionDonacionesUseCase {
    RecepcionDonacion recibir(CargaBienesRequestDTO requestDTO);
}
