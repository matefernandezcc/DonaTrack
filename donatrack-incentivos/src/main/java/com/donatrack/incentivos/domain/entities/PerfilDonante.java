package com.donatrack.incentivos.domain.entities;

import com.donatrack.incentivos.domain.entities.categoria.CategoriaDonante;
import com.donatrack.incentivos.domain.entities.misiones.*;
import java.util.Queue;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.YearMonth;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerfilDonante {
    private UUID donanteId;
    private CategoriaDonante categoria;
    private List<Insignia> insigniasObtenidas;
    private Queue<Mision> misionesPendientes;
    private Mision misionActual;
    private MetricasDonante metricas;

    public PerfilDonante(UUID donanteId) {
        this.donanteId = donanteId;
        this.insigniasObtenidas = new ArrayList<>();
        this.metricas = new MetricasDonante(donanteId);
        this.categoria = CategoriaDonante.COLABORADOR;
        cargarMisionesDeCategoriaActual();
    }

    public void cargarMisionesDeCategoriaActual() {
        this.misionesPendientes = MisionesFactory.crearMisionesPara(this.categoria);
        this.misionActual = misionesPendientes.poll();
    }

    public void registrarDonacionExitosa(RegistroDonacion donacion) {
        this.metricas.registrarDonacion(donacion);
        evaluarMisiones(donacion.getMesDonacion());
    }

    /**
     * Procesa la inactividad del donante verificando si pasaron más de 30 días
     * desde su última donación. Si es así, se resetean los registros (racha cortada).
     *
     * Corrección: antes se comparaba por YearMonth (mes calendario), ahora se usa
     * LocalDate para calcular exactamente 30 días como pidió el profesor.
     *
     * @param fechaActual la fecha actual contra la cual se evalúa la inactividad
     */
    public void procesarInactividad(java.time.LocalDate fechaActual) {
        List<RegistroDonacion> donaciones = metricas.obtenerTodasLasDonaciones();
        if (donaciones.isEmpty()) {
            return;
        }

        // Buscar la fecha exacta de la donación más reciente
        java.time.LocalDate ultimaFecha = donaciones.stream()
                .map(RegistroDonacion::getFechaDonacion)
                .filter(f -> f != null)
                .max(java.time.LocalDate::compareTo)
                .orElse(null);

        if (ultimaFecha == null) {
            return;
        }

        // Si pasaron más de 30 días desde la última donación, se corta la racha
        long diasSinDonar = java.time.temporal.ChronoUnit.DAYS.between(ultimaFecha, fechaActual);
        if (diasSinDonar > 30) {
            this.metricas.getRegistrosDonacion().clear();
        }
    }

    public void evaluarMisiones(YearMonth mesActual) {
        while ((misionActual != null) && (misionActual.evaluar(this))) {
            completarMisionActual(mesActual);
        }
    }

    private void completarMisionActual(YearMonth mesActual) {
        this.metricas.registrarMisionCompletada(this.misionActual, mesActual);
        agregarInsignia(misionActual.getRecompensa());

        misionActual = misionesPendientes.poll();

        if (misionActual == null) {
            avanzarCategoria();
        }
    }

    private void avanzarCategoria() {
        this.categoria = calcularSiguienteCategoria(this.categoria);
        cargarMisionesDeCategoriaActual();
    }

    private CategoriaDonante calcularSiguienteCategoria(CategoriaDonante actual) {
        switch (actual) {
            case COLABORADOR:
                return CategoriaDonante.SOSTENEDOR;
            case SOSTENEDOR:
            case TRANSFORMADOR:
                return CategoriaDonante.TRANSFORMADOR;
            default:
                return actual;
        }
    }

    private void agregarInsignia(Insignia insignia) {
        if (insignia != null) {
            this.insigniasObtenidas.add(insignia);
        }
    }
}
