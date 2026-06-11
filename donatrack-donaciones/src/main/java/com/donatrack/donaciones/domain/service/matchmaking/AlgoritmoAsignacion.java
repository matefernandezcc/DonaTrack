package com.donatrack.donaciones.domain.service.matchmaking;

import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.model.roles.Beneficiario;

import java.util.List;

public interface AlgoritmoAsignacion {

    /**
     * Evalúa a las entidades beneficiarias y genera un ranking (hasta 10 entidades)
     * en función de qué tanto corresponde que reciban la donación.
     *
     * @param donacion La donación que se desea asignar.
     * @param beneficiariosDisponibles La lista de beneficiarios candidatos.
     * @return Lista rankeada de hasta 10 beneficiarios.
     */
    List<Beneficiario> sugerirBeneficiarios(Donacion donacion, List<Beneficiario> beneficiariosDisponibles);
}
