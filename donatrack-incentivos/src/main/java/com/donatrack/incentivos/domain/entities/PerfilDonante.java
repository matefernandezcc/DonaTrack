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

    public void procesarInactividad(YearMonth mesActual) {
        List<RegistroDonacion> donaciones = metricas.obtenerTodasLasDonaciones();
        if (donaciones.isEmpty()) {
            return;
        }

        // Buscar el mes de la donación más reciente
        YearMonth ultimoMes = donaciones.stream()
                .map(RegistroDonacion::getMesDonacion)
                .max(YearMonth::compareTo)
                .orElse(mesActual);

        // Si pasó más de un mes completo de diferencia, se resetean los registros (racha cortada)
        if (ultimoMes.plusMonths(1).isBefore(mesActual)) {
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
        if (this.categoria == CategoriaDonante.COLABORADOR) {
            this.categoria = CategoriaDonante.SOSTENEDOR;
        } else if (this.categoria == CategoriaDonante.SOSTENEDOR) {
            this.categoria = CategoriaDonante.TRANSFORMADOR;
        }
        cargarMisionesDeCategoriaActual();
    }

    private void agregarInsignia(Insignia insignia) {
        if (insignia != null) {
            this.insigniasObtenidas.add(insignia);
        }
    }
}
