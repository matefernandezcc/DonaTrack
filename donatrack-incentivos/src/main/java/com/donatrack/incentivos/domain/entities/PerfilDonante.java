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
    private MetricasDonante metricas; // Métricas

    public PerfilDonante(UUID donanteId) {
        this.donanteId = donanteId;
        this.insigniasObtenidas = new ArrayList<>();
        this.metricas = new MetricasDonante();
        this.categoria = CategoriaDonante.COLABORADOR;
        cargarMisionesDeCategoriaActual();
    }

    public void cargarMisionesDeCategoriaActual() {
        this.misionesPendientes = MisionesFactory.crearMisionesPara(this.categoria);
        this.misionActual = misionesPendientes.poll();
    }

    public void registrarDonacion(RegistroDonacion donacion) {
        this.metricas.registrarDonacion(donacion);
        evaluarMisiones(donacion.getMesDonacion());
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
        this.categoria = this.categoria.siguienteNivel();
        cargarMisionesDeCategoriaActual();
    }

    private void agregarInsignia(Insignia insignia) {
        if (insignia != null) {
            this.insigniasObtenidas.add(insignia);
        }
    }
}
