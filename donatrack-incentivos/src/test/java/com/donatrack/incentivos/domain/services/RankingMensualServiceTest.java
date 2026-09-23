package com.donatrack.incentivos.domain.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.donatrack.incentivos.application.ports.out.PerfilDonanteRepository;
import com.donatrack.incentivos.domain.entities.MetricasDonante;
import com.donatrack.incentivos.domain.entities.PerfilDonante;
import com.donatrack.incentivos.domain.entities.RegistroDonacion;
import com.donatrack.incentivos.domain.entities.misiones.Mision;
import com.donatrack.incentivos.domain.entities.misiones.TipoMetricaMision;
import com.donatrack.incentivos.domain.entities.Insignia;
import com.donatrack.incentivos.domain.entities.ranking.RankingMisionesStrategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Tests unitarios del RankingMensualService.
 *
 * Usa Mockito para mockear el PerfilDonanteRepository.
 * La strategy se instancia real (RankingMisionesStrategy) porque es pura lógica de dominio.
 */
public class RankingMensualServiceTest {

    private PerfilDonanteRepository mockRepository;
    private RankingMensualService service;
    private YearMonth mes;

    @BeforeEach
    void setUp() {
        mockRepository = mock(PerfilDonanteRepository.class);
        // Usamos RankingMisionesStrategy real: calcula puntaje = cant. misiones completadas en el mes
        service = new RankingMensualService(mockRepository, new RankingMisionesStrategy());
        mes = YearMonth.of(2026, 8);
    }

    @Test
    public void testObtenerTop3OrdenaPorPuntajeDescendente() {
        // Crear 4 perfiles con distintas cantidades de misiones completadas en agosto
        PerfilDonante p1 = crearPerfilConMisiones(1, mes);
        PerfilDonante p2 = crearPerfilConMisiones(3, mes);
        PerfilDonante p3 = crearPerfilConMisiones(2, mes);
        PerfilDonante p4 = crearPerfilConMisiones(0, mes);

        when(mockRepository.findAll()).thenReturn(List.of(p1, p2, p3, p4));

        List<PerfilDonante> top3 = service.obtenerTop3Mensual(mes);

        assertEquals(3, top3.size());
        assertEquals(p2, top3.get(0), "El primero debería ser el que tiene 3 misiones");
        assertEquals(p3, top3.get(1), "El segundo debería ser el que tiene 2 misiones");
        assertEquals(p1, top3.get(2), "El tercero debería ser el que tiene 1 misión");
    }

    @Test
    public void testObtenerTop3ConMenosDe3Perfiles() {
        PerfilDonante p1 = crearPerfilConMisiones(1, mes);

        when(mockRepository.findAll()).thenReturn(List.of(p1));

        List<PerfilDonante> top3 = service.obtenerTop3Mensual(mes);

        assertEquals(1, top3.size());
    }

    @Test
    public void testObtenerTop3ConListaVacia() {
        when(mockRepository.findAll()).thenReturn(List.of());

        List<PerfilDonante> top3 = service.obtenerTop3Mensual(mes);

        assertTrue(top3.isEmpty());
    }

    // ========================= Helper =========================

    /**
     * Crea un PerfilDonante con N misiones completadas en el mes indicado.
     * Registra directamente en las métricas del perfil.
     */
    private PerfilDonante crearPerfilConMisiones(int cantMisiones, YearMonth enMes) {
        PerfilDonante perfil = new PerfilDonante(UUID.randomUUID());
        MetricasDonante metricas = perfil.getMetricas();

        for (int i = 0; i < cantMisiones; i++) {
            Mision mision = new Mision("Mision-" + i,
                    new Insignia("Badge-" + i, "Desc"),
                    TipoMetricaMision.DONACIONES_EXITOSAS, 1);
            metricas.registrarMisionCompletada(mision, enMes);
        }

        return perfil;
    }
}
