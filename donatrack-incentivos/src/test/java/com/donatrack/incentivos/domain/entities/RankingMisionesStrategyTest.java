package com.donatrack.incentivos.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.donatrack.incentivos.domain.entities.misiones.Mision;
import com.donatrack.incentivos.domain.entities.misiones.TipoMetricaMision;
import com.donatrack.incentivos.domain.entities.ranking.RankingMisionesStrategy;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.UUID;

/**
 * Tests unitarios de RankingMisionesStrategy.
 *
 * Esta strategy calcula el puntaje de un donante basándose en la cantidad
 * de misiones completadas en un mes dado.
 */
public class RankingMisionesStrategyTest {

    @Test
    public void testCalcularPuntajeConMisionesCompletadas() {
        RankingMisionesStrategy strategy = new RankingMisionesStrategy();
        YearMonth mes = YearMonth.of(2026, 8);

        PerfilDonante perfil = new PerfilDonante(UUID.randomUUID());

        // Registrar 2 misiones completadas en agosto
        Mision m1 = new Mision("M1", new Insignia("I1", "D1"), TipoMetricaMision.DONACIONES_EXITOSAS, 1);
        Mision m2 = new Mision("M2", new Insignia("I2", "D2"), TipoMetricaMision.MAX_BIENES, 3);
        perfil.getMetricas().registrarMisionCompletada(m1, mes);
        perfil.getMetricas().registrarMisionCompletada(m2, mes);

        double puntaje = strategy.calcularPuntaje(perfil, mes);
        assertEquals(2.0, puntaje, "Debería contar 2 misiones completadas en agosto");
    }

    @Test
    public void testCalcularPuntajeSinMisiones() {
        RankingMisionesStrategy strategy = new RankingMisionesStrategy();
        PerfilDonante perfil = new PerfilDonante(UUID.randomUUID());

        double puntaje = strategy.calcularPuntaje(perfil, YearMonth.of(2026, 8));
        assertEquals(0.0, puntaje);
    }

    @Test
    public void testCalcularPuntajeConMetricasNull() {
        RankingMisionesStrategy strategy = new RankingMisionesStrategy();
        PerfilDonante perfil = new PerfilDonante(UUID.randomUUID());
        perfil.setMetricas(null);

        double puntaje = strategy.calcularPuntaje(perfil, YearMonth.of(2026, 8));
        assertEquals(0.0, puntaje);
    }

    @Test
    public void testPuntajeSoloConsideraMesIndicado() {
        RankingMisionesStrategy strategy = new RankingMisionesStrategy();
        PerfilDonante perfil = new PerfilDonante(UUID.randomUUID());

        YearMonth agosto = YearMonth.of(2026, 8);
        YearMonth julio = YearMonth.of(2026, 7);

        Mision m1 = new Mision("M1", new Insignia("I1", "D1"), TipoMetricaMision.DONACIONES_EXITOSAS, 1);
        Mision m2 = new Mision("M2", new Insignia("I2", "D2"), TipoMetricaMision.MAX_BIENES, 1);

        perfil.getMetricas().registrarMisionCompletada(m1, agosto);
        perfil.getMetricas().registrarMisionCompletada(m2, julio);

        // Solo debe contar la de agosto
        assertEquals(1.0, strategy.calcularPuntaje(perfil, agosto));
        assertEquals(1.0, strategy.calcularPuntaje(perfil, julio));
    }
}
