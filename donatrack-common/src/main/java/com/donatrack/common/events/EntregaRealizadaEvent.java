package com.donatrack.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntregaRealizadaEvent {
    private UUID idDonacion;
    private List<String> fotos;
    private String patenteCamion;
    private LocalDateTime fechaHora;
}
