package com.donatrack.incentivos.domain.entities;

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
    private UUID idDonacion;
    private int cantidadBienes;
    private Set<String> categorias;
    private UUID idEntidadBeneficiaria;
    private YearMonth mesDonacion;

    public boolean esExitosa() {
        return idEntidadBeneficiaria != null;
    }
}
