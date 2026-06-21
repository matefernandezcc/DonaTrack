package com.donatrack.common.events;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntregaConfirmadaEvent {
    private UUID idDonacionOriginal;
    private List<String> fotos;
    private String camionPatente;
}
