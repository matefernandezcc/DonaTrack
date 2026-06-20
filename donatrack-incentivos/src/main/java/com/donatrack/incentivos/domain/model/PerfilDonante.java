package com.donatrack.incentivos.domain.model;

import com.donatrack.incentivos.domain.model.categoria.CategoriaDonanteState;
import com.donatrack.incentivos.domain.model.categoria.ColaboradorState;
import com.donatrack.incentivos.domain.model.misiones.Mision;
import com.donatrack.incentivos.domain.model.misiones.MisionesPorNivel;
import com.donatrack.common.dto.ActividadDonacionDTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.time.YearMonth;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerfilDonante {
    private UUID donanteId;
    private CategoriaDonanteState categoria;
    private List<Insignia> insigniasObtenidas;
    private MisionesPorNivel misionesPorNivel;
    private Mision misionActual;

    // Métricas
    private int totalDonacionesHistoricas;
    private int mesesConsecutivosDonando;
    private int cantidadBienesDonados;
    private int donacionesExitosas;
    private int maxBienesEnUnaDonacion;
    private Set<String> categoriasUnicasDonadas;

    // Historial para gráficas y analíticas
    private Set<UUID> organizacionesUnicasAyudadas;
    private Map<YearMonth, Integer> historialDonacionesPorMes;
    private YearMonth ultimoMesDonacion;

    public PerfilDonante(UUID donanteId) {
        this.donanteId = donanteId;
        this.insigniasObtenidas = new ArrayList<>();
        this.organizacionesUnicasAyudadas = new HashSet<>();
        this.historialDonacionesPorMes = new HashMap<>();
        this.categoriasUnicasDonadas = new HashSet<>();
        this.maxBienesEnUnaDonacion = 0;

        // Inicializar categoría y misiones (State y Factory)
        this.categoria = new ColaboradorState(this);
        cargarMisionesDeCategoriaActual();
    }

    public void cargarMisionesDeCategoriaActual() {
        this.misionesPorNivel = ConfiguracionRecompensasFactory
                .obtenerMisionesPara(this, this.categoria.getValorEnum());
        if (this.misionesPorNivel != null) {
            this.misionActual = this.misionesPorNivel.getMisionActual();
        } else {
            this.misionActual = null;
        }
    }

    public void registrarDonacionExitosa(ActividadDonacionDTO actividad) {
        this.donacionesExitosas++;
        this.totalDonacionesHistoricas++;
        this.cantidadBienesDonados += actividad.getCantidadBienes();

        if (actividad.getIdEntidadBeneficiaria() != null) {
            this.organizacionesUnicasAyudadas.add(actividad.getIdEntidadBeneficiaria());
        }

        if (actividad.getCantidadBienes() > this.maxBienesEnUnaDonacion) {
            this.maxBienesEnUnaDonacion = actividad.getCantidadBienes();
        }

        if (actividad.getCategorias() != null) {
            this.categoriasUnicasDonadas.addAll(actividad.getCategorias());
        }

        YearMonth mesActual = YearMonth.from(actividad.getFecha());
        historialDonacionesPorMes.put(mesActual, historialDonacionesPorMes.getOrDefault(mesActual, 0) + 1);

        if (ultimoMesDonacion == null || ultimoMesDonacion.plusMonths(1).equals(mesActual)) {
            // Aumenta racha si es el primer mes o es un mes exactamente posterior
            if (ultimoMesDonacion != null && !ultimoMesDonacion.equals(mesActual)) {
                this.mesesConsecutivosDonando++;
            } else if (ultimoMesDonacion == null) {
                this.mesesConsecutivosDonando = 1;
            }
        } else if (mesActual.isAfter(ultimoMesDonacion.plusMonths(1))) {
            // Perdió la racha
            this.mesesConsecutivosDonando = 1;
        }

        this.ultimoMesDonacion = mesActual;

        evaluarMisiones();
    }

    public void evaluarMisiones() {
        if ((misionActual != null) && (misionActual.evaluar(this))) {
            agregarInsignia(misionActual.getRecompensa());
            // Obtener siguiente misión
            if (misionesPorNivel != null) {
                misionActual = misionesPorNivel.avanzarMision();
            } else {
                misionActual = null;
            }
            // Si ya no hay misiones, completó la categoría
            if (misionActual == null) {
                categoria.avanzarCategoria();
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
