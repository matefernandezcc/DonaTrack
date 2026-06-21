package com.donatrack.common.events;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntregaFallidaEvent {
    private UUID idDonacionOriginal;
    private String motivo;
}
