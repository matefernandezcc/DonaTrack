package com.donatrack.incentivos.domain.model;

import com.donatrack.incentivos.domain.model.categoria.CategoriaDonante;
import com.donatrack.incentivos.domain.model.misiones.Mision;
import java.util.Queue;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

        // Inicializar categoría y misiones (State y Factory)
        this.categoria = CategoriaDonante.COLABORADOR;
        cargarMisionesDeCategoriaActual();
    }

    public void cargarMisionesDeCategoriaActual() {
        this.misionesPendientes = MisionesFactory.crearMisionesPara(this.categoria);
        this.misionActual = misionesPendientes.poll();
    }

    public void registrarDonacionExitosa(int cantidadBienes, Set<String> categorias, UUID idEntidadBeneficiaria,
            YearMonth mesDonacion) {
        this.metricas.registrarDonacionExitosa(cantidadBienes, categorias, idEntidadBeneficiaria, mesDonacion);
        evaluarMisiones(mesDonacion);
    }

    public void evaluarMisiones(YearMonth mesActual) {
        if ((misionActual != null) && (misionActual.evaluar(this))) {
            this.metricas.registrarMisionCompletada(mesActual);
            agregarInsignia(misionActual.getRecompensa());
            // Obtener siguiente misión
            misionActual = misionesPendientes.poll();
            // Si ya no hay misiones, completó la categoría
            if (misionActual == null) {
                this.categoria = this.categoria.siguienteNivel();
                cargarMisionesDeCategoriaActual();
            }
        }
    }

    public List<Mision> getMisionesActivas() {
        // Para compatibilidad con el frontend, devolvemos la misión actual como lista
        // de 1 elemento
        if (misionActual != null) {
            return List.of(misionActual);
        }
        return new ArrayList<>();
    }

    private void agregarInsignia(Insignia insignia) {
        if (insignia != null) {
            this.insigniasObtenidas.add(insignia);
        }
    }
}
