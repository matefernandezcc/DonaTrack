package com.donatrack.common.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActividadDonacionDTO {
    private UUID idDonacion;
    private UUID idDonante;
    private int cantidadBienes;
    private List<String> categorias;
    private UUID idEntidadBeneficiaria;
    private LocalDate fecha;
}
