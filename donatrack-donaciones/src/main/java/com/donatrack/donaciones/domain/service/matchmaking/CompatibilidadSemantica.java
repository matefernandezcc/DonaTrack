package com.donatrack.donaciones.domain.service.matchmaking;

import com.donatrack.donaciones.domain.model.Beneficiario;
import com.donatrack.donaciones.domain.model.Donacion;
import java.util.List;

public class CompatibilidadSemantica implements AlgoritmoAsignacion {

    @Override
    public List<Beneficiario> sugerirBeneficiarios(Donacion donacion, List<Beneficiario> beneficiariosDisponibles) {
        // Implementación dummy para la iteración actual.
        // Aquí se debe analizar la correspondencia entre los bienes donados
        // y las necesidades declaradas por cada entidad.
        return beneficiariosDisponibles.stream()
            // .filter(b -> coincideNecesidad(b, donacion)) // Logica a implementar
            .limit(10)
            .toList();
    }
}
