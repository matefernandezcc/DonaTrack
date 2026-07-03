package com.donatrack.incentivos.domain.entities.misiones;

import java.util.UUID;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.time.YearMonth;

import com.donatrack.incentivos.domain.entities.Insignia;
import com.donatrack.incentivos.domain.entities.PerfilDonante;
import com.donatrack.incentivos.domain.entities.RegistroDonacion;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Mision {
    private UUID id = UUID.randomUUID();
    private String nombre;
    private Insignia recompensa;
    private TipoMetricaMision tipoMetrica;
    private int objetivo;

    public Mision(String nombre, Insignia recompensa, TipoMetricaMision tipoMetrica, int objetivo) {
        this.nombre = nombre;
        this.recompensa = recompensa;
        this.tipoMetrica = tipoMetrica;
        this.objetivo = objetivo;
    }

    public boolean evaluar(PerfilDonante perfil) {
        return extraerValor(perfil) >= objetivo;
    }

    public String getProgresoActual(PerfilDonante perfil) {
        return extraerValor(perfil) + " / " + objetivo;
    }

    private int extraerValor(PerfilDonante perfil) {
        if (perfil.getMetricas() == null) {
            return 0;
        }

        switch (this.tipoMetrica) {
            case DONACIONES_EXITOSAS:
                return perfil.getMetricas().obtenerDonacionesExitosas().size();

            case MAX_BIENES:
                return perfil.getMetricas().obtenerTodasLasDonaciones().stream()
                        .mapToInt(RegistroDonacion::getCantidadBienes)
                        .max()
                        .orElse(0);

            case MESES_CONSECUTIVOS:
                List<RegistroDonacion> donaciones = perfil.getMetricas().obtenerTodasLasDonaciones();
                if (donaciones.isEmpty()) {
                    return 0;
                }
                List<YearMonth> meses = donaciones.stream()
                        .map(RegistroDonacion::getMesDonacion)
                        .distinct()
                        .sorted(Comparator.reverseOrder())
                        .collect(Collectors.toList());

                // Si no hay donaciones en el mes actual (o último mes en curso), racha es 0
                YearMonth mesActual = YearMonth.now();
                if (!meses.contains(mesActual) && !meses.contains(mesActual.minusMonths(1))) {
                    return 0;
                }

                int racha = 1;
                for (int i = 0; i < meses.size() - 1; i++) {
                    if (meses.get(i).minusMonths(1).equals(meses.get(i + 1))) {
                        racha++;
                    } else {
                        break;
                    }
                }
                return racha;

            case CATEGORIAS_DISTINTAS:
                Set<String> cats = perfil.getMetricas().obtenerTodasLasDonaciones().stream()
                        .map(RegistroDonacion::getCategorias)
                        .filter(Objects::nonNull)
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet());
                return cats.size();

            default:
                return 0;
        }
    }
}
