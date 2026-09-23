package com.donatrack.incentivos.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.donatrack.incentivos.domain.entities.misiones.Mision;
import com.donatrack.incentivos.domain.entities.misiones.TipoMetricaMision;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MetricasDonanteTest {

    private MetricasDonante metricas;

    @BeforeEach
    void setUp() {
        metricas = new MetricasDonante(UUID.randomUUID());
    }

    @Test
    public void testRegistrarDonacionAgregaNueva() {
        RegistroDonacion donacion = crearDonacion(UUID.randomUUID(), true);
        metricas.registrarDonacion(donacion);

        assertEquals(1, metricas.obtenerTodasLasDonaciones().size());
    }

    @Test
    public void testRegistrarDonacionConIdDuplicadoActualiza() {
        UUID idDonacion = UUID.randomUUID();

        RegistroDonacion donacion1 = new RegistroDonacion(idDonacion, 3, Set.of("Ropa"), UUID.randomUUID(),
                YearMonth.now());
        RegistroDonacion donacion2 = new RegistroDonacion(idDonacion, 5, Set.of("Ropa", "Alimentos"),
                UUID.randomUUID(), YearMonth.now());

        metricas.registrarDonacion(donacion1);
        metricas.registrarDonacion(donacion2);

        // No debería duplicar, debería actualizar
        assertEquals(1, metricas.obtenerTodasLasDonaciones().size());
        assertEquals(5, metricas.obtenerTodasLasDonaciones().get(0).getCantidadBienes());
    }

    @Test
    public void testObtenerDonacionesExitosasFiltraCorrectamente() {
        metricas.registrarDonacion(crearDonacion(UUID.randomUUID(), true));
        metricas.registrarDonacion(crearDonacion(UUID.randomUUID(), false));
        metricas.registrarDonacion(crearDonacion(UUID.randomUUID(), true));

        List<RegistroDonacion> exitosas = metricas.obtenerDonacionesExitosas();

        assertEquals(2, exitosas.size());
    }

    @Test
    public void testRegistrarMisionCompletadaYObtenerPorMes() {
        Mision mision = new Mision("Test", new Insignia("Test", "Test"),
                TipoMetricaMision.DONACIONES_EXITOSAS, 1);

        YearMonth agosto = YearMonth.of(2026, 8);
        metricas.registrarMisionCompletada(mision, agosto);

        List<Mision> completadas = metricas.obtenerMisionesCompletadasEn(agosto);
        assertEquals(1, completadas.size());
        assertEquals(mision, completadas.get(0));

        // Otro mes no debería tener misiones
        List<Mision> septiembre = metricas.obtenerMisionesCompletadasEn(YearMonth.of(2026, 9));
        assertTrue(septiembre.isEmpty());
    }

    @Test
    public void testObtenerTodasLasDonacionesRetornaCopia() {
        metricas.registrarDonacion(crearDonacion(UUID.randomUUID(), true));

        List<RegistroDonacion> lista = metricas.obtenerTodasLasDonaciones();
        lista.clear(); // Modificar la copia no debería afectar la original

        assertEquals(1, metricas.obtenerTodasLasDonaciones().size());
    }

    // ========================= Helper =========================

    private RegistroDonacion crearDonacion(UUID id, boolean exitosa) {
        return new RegistroDonacion(id, 1, Set.of("General"),
                exitosa ? UUID.randomUUID() : null, YearMonth.now());
    }
}
