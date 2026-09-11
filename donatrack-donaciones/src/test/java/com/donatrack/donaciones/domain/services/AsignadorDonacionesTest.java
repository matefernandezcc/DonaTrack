package com.donatrack.donaciones.domain.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.Subcategoria;
import com.donatrack.donaciones.domain.entities.necesidades.NecesidadExtraordinaria;
import com.donatrack.donaciones.domain.entities.roles.strategyAdministrador.asignador.AlgoritmoAsignacion;

import org.junit.jupiter.api.Test;

import java.util.List;

public class AsignadorDonacionesTest {

    @Test
    public void testDelegaAlAlgoritmoInyectado() {
        Subcategoria sub = new Subcategoria("Ropa", "Ropa");
        Donacion donacion = new Donacion(sub);
        NecesidadExtraordinaria necesidad = new NecesidadExtraordinaria("N1", sub, 10);

        ResultadoMatch matchEsperado = new ResultadoMatch(donacion, null, necesidad);

        AlgoritmoAsignacion mockAlgoritmo = mock(AlgoritmoAsignacion.class);
        when(mockAlgoritmo.recomendarNecesidades(anyList(), anyList()))
                .thenReturn(List.of(matchEsperado));

        AsignadorDonaciones asignador = new AsignadorDonaciones(mockAlgoritmo);

        List<ResultadoMatch> resultado = asignador.ejecutarMatchmaking(
                List.of(donacion), List.of(necesidad));

        assertEquals(1, resultado.size());
        assertEquals(matchEsperado, resultado.get(0));
        verify(mockAlgoritmo).recomendarNecesidades(anyList(), anyList());
    }

    @Test
    public void testResultadosDelAlgoritmoSePropagan() {
        Subcategoria sub = new Subcategoria("General", "General");
        Donacion d1 = new Donacion(sub);
        Donacion d2 = new Donacion(sub);
        NecesidadExtraordinaria n1 = new NecesidadExtraordinaria("N1", sub, 10);
        NecesidadExtraordinaria n2 = new NecesidadExtraordinaria("N2", sub, 20);

        AlgoritmoAsignacion mockAlgoritmo = mock(AlgoritmoAsignacion.class);
        when(mockAlgoritmo.recomendarNecesidades(anyList(), anyList()))
                .thenReturn(List.of(
                        new ResultadoMatch(d1, null, n1),
                        new ResultadoMatch(d2, null, n2)));

        AsignadorDonaciones asignador = new AsignadorDonaciones(mockAlgoritmo);

        List<ResultadoMatch> resultado = asignador.ejecutarMatchmaking(
                List.of(d1, d2), List.of(n1, n2));

        assertEquals(2, resultado.size());
    }
}
