package com.donatrack.incentivos.domain.model;

import java.time.YearMonth;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroDonacion {
    private int cantidadBienes;
    private Set<String> categorias;
    private UUID idEntidadBeneficiaria;
    private YearMonth mesDonacion;
}
