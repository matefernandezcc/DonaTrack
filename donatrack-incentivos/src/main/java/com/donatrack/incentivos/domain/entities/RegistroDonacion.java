package com.donatrack.incentivos.domain.entities;

import java.time.LocalDate;
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
    /**
     * Fecha exacta de la donación. Permite calcular inactividad por 30 días calendario
     * en lugar de por mes (requerimiento del profesor: "si pasaron 30 días sin donar").
     */
    private LocalDate fechaDonacion;

    public RegistroDonacion(UUID idDonacion, int cantidadBienes, Set<String> categorias,
                            UUID idEntidadBeneficiaria, YearMonth mesDonacion) {
        this.idDonacion = idDonacion;
        this.cantidadBienes = cantidadBienes;
        this.categorias = categorias;
        this.idEntidadBeneficiaria = idEntidadBeneficiaria;
        this.mesDonacion = mesDonacion;
        // Por defecto, usar el primer día del mes si no se especifica fecha exacta
        this.fechaDonacion = mesDonacion != null ? mesDonacion.atDay(1) : null;
    }

    public boolean esExitosa() {
        return idEntidadBeneficiaria != null;
    }
}
