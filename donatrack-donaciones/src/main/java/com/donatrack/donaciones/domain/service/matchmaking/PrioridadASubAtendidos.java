package com.donatrack.donaciones.domain.service.matchmaking;

import com.donatrack.donaciones.domain.model.Beneficiario;
import com.donatrack.donaciones.domain.model.Donacion;
import java.util.List;

public class PrioridadASubAtendidos implements AlgoritmoAsignacion {

    @Override
    public List<Beneficiario> sugerirBeneficiarios(Donacion donacion, List<Beneficiario> beneficiariosDisponibles) {
        // Implementación dummy.
        // Aquí se asigna prioridad a organizaciones que hayan recibido menos 
        // donaciones en el último trimestre.
        return beneficiariosDisponibles.stream()
            // .sorted(Comparator.comparingInt(b -> b.getDonacionesRecibidasUltimoTrimestre()))
            .limit(10)
            .toList();
    }
}
