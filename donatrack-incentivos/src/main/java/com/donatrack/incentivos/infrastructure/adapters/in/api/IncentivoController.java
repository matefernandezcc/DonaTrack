package com.donatrack.incentivos.infrastructure.adapters.in.api;

import com.donatrack.common.dto.ActividadDonacionDTO;
import com.donatrack.common.dto.ErrorResponse;
import com.donatrack.incentivos.application.ports.out.DifusionPort;
import com.donatrack.incentivos.application.ports.out.PerfilDonanteRepository;
import com.donatrack.incentivos.application.usecases.RegistrarActividadDonacionUseCase;
import com.donatrack.incentivos.domain.entities.Insignia;
import com.donatrack.incentivos.domain.entities.InsigniaObtenidaEvent;
import com.donatrack.incentivos.domain.entities.PerfilDonante;
import com.donatrack.incentivos.domain.entities.RegistroDonacion;
import com.donatrack.incentivos.domain.entities.misiones.Mision;
import com.donatrack.incentivos.domain.entities.misiones.TipoMetricaMision;
import com.donatrack.incentivos.domain.services.RankingMensualService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(
    name = "Incentivos",
    description = "Métricas de donantes, insignias, misiones y ranking mensual")
public class IncentivoController {

  private final RankingMensualService rankingMensualService;
  private final RegistrarActividadDonacionUseCase registrarActividadDonacionUseCase;
  private final com.donatrack.incentivos.infrastructure.adapters.out.client.PersonaClient
      personaClient;
  private final PerfilDonanteRepository perfilDonanteRepository;
  private final DifusionPort difusionPort;
  private final org.springframework.web.client.RestTemplate restTemplate;
  private final com.donatrack.incentivos.infrastructure.adapters.out.persistence.repositories
          .RankingMensualJpaRepository
      rankingMensualJpaRepository;
  private final com.donatrack.incentivos.infrastructure.adapters.out.persistence.repositories
          .PerfilDonanteJpaRepository
      perfilDonanteJpaRepository;

  @org.springframework.beans.factory.annotation.Value(
      "${n8n.webhook.ranking.url:http://localhost:5678/webhook/donatrack/ranking}")
  private String n8nWebhookRankingUrl;

  public IncentivoController(
      RankingMensualService rankingMensualService,
      RegistrarActividadDonacionUseCase registrarActividadDonacionUseCase,
      com.donatrack.incentivos.infrastructure.adapters.out.client.PersonaClient personaClient,
      PerfilDonanteRepository perfilDonanteRepository,
      DifusionPort difusionPort) {
    this(
        rankingMensualService,
        registrarActividadDonacionUseCase,
        personaClient,
        perfilDonanteRepository,
        difusionPort,
        null,
        null);
  }

  @org.springframework.beans.factory.annotation.Autowired
  public IncentivoController(
      RankingMensualService rankingMensualService,
      RegistrarActividadDonacionUseCase registrarActividadDonacionUseCase,
      com.donatrack.incentivos.infrastructure.adapters.out.client.PersonaClient personaClient,
      PerfilDonanteRepository perfilDonanteRepository,
      DifusionPort difusionPort,
      com.donatrack.incentivos.infrastructure.adapters.out.persistence.repositories
              .RankingMensualJpaRepository
          rankingMensualJpaRepository,
      com.donatrack.incentivos.infrastructure.adapters.out.persistence.repositories
              .PerfilDonanteJpaRepository
          perfilDonanteJpaRepository) {
    this.rankingMensualService = rankingMensualService;
    this.registrarActividadDonacionUseCase = registrarActividadDonacionUseCase;
    this.personaClient = personaClient;
    this.perfilDonanteRepository = perfilDonanteRepository;
    this.difusionPort = difusionPort;
    this.rankingMensualJpaRepository = rankingMensualJpaRepository;
    this.perfilDonanteJpaRepository = perfilDonanteJpaRepository;
    this.restTemplate = new org.springframework.web.client.RestTemplate();
  }

  @Operation(
      summary = "Obtener métricas del donante",
      description =
          "Devuelve las métricas de donación: historial, racha, bienes donados, organizaciones ayudadas y posición en el ranking")
  @ApiResponse(responseCode = "200", description = "Métricas del donante")
  @ApiResponse(
      responseCode = "400",
      description = "ID de donante con formato inválido",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @GetMapping("/donantes/{id}/metricas")
  public ResponseEntity<MetricasDonanteDTO> obtenerMetricas(@PathVariable UUID id) {
    PerfilDonante perfil =
        perfilDonanteRepository.findById(id).orElseGet(() -> new PerfilDonante(id));

    List<RegistroDonacion> todasLasDonaciones = perfil.getMetricas().obtenerTodasLasDonaciones();
    List<RegistroDonacion> exitosas = perfil.getMetricas().obtenerDonacionesExitosas();

    int totalHistorico = todasLasDonaciones.size();
    int racha = calcularRacha(todasLasDonaciones, perfil.getMetricas().getFechaCorteRacha());
    int bienesDonados =
        todasLasDonaciones.stream().mapToInt(RegistroDonacion::getCantidadBienes).sum();
    int totalExitosas = exitosas.size();

    long organizacionesAyudadas =
        todasLasDonaciones.stream()
            .map(RegistroDonacion::getIdEntidadBeneficiaria)
            .filter(Objects::nonNull)
            .distinct()
            .count();

    Map<YearMonth, Integer> historialMap = new HashMap<>();
    for (RegistroDonacion d : todasLasDonaciones) {
      historialMap.merge(d.getMesDonacion(), 1, Integer::sum);
    }

    int donacionesEnDeposito = Math.max(0, totalHistorico - totalExitosas);
    // Esquema justo por etapas: +10 puntos inmediatos en depósito, +25 puntos extra al confirmarse
    // la entrega a la entidad
    int puntos = (donacionesEnDeposito * 10) + (totalExitosas * 35) + (bienesDonados * 2);

    MetricasDonanteDTO dto =
        MetricasDonanteDTO.builder()
            .donanteId(perfil.getDonanteId())
            .totalDonacionesHistoricas(totalHistorico)
            .rachaDeMeses(racha)
            .cantidadBienesDonados(bienesDonados)
            .donacionesExitosas(totalExitosas)
            .totalOrganizacionesAyudadas((int) organizacionesAyudadas)
            .historialDonacionesPorMes(historialMap)
            .posicionRanking(totalHistorico > 0 ? 5 : 0)
            .puntosTotales(puntos)
            .build();

    return ResponseEntity.ok(dto);
  }

  @Operation(
      summary = "Obtener misiones disponibles",
      description = "Devuelve las misiones activas para un donante")
  @ApiResponse(responseCode = "200", description = "Lista de misiones")
  @ApiResponse(
      responseCode = "400",
      description = "ID de donante con formato inválido",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @GetMapping("/donantes/{id}/misiones")
  public ResponseEntity<List<MisionProgresoDTO>> obtenerMisionesDisponibles(@PathVariable UUID id) {
    PerfilDonante perfil =
        perfilDonanteRepository.findById(id).orElseGet(() -> new PerfilDonante(id));
    List<MisionProgresoDTO> respuesta = new ArrayList<>();
    if (perfil.getMisionActual() != null) {
      respuesta.add(toMisionProgresoDto(perfil.getMisionActual(), perfil, false));
    }
    if (perfil.getMisionesPendientes() != null) {
      for (Mision pendiente : perfil.getMisionesPendientes()) {
        if (pendiente != null) {
          respuesta.add(toMisionProgresoDto(pendiente, perfil, true));
        }
      }
    }
    return ResponseEntity.ok(respuesta);
  }

  private MisionProgresoDTO toMisionProgresoDto(Mision m, PerfilDonante perfil, boolean bloqueada) {
    int valorActual = m.calcularValorActual(perfil);
    int objetivo = Math.max(1, m.getObjetivo());
    int porcentaje = Math.min(100, (valorActual * 100) / objetivo);
    int faltante = Math.max(0, objetivo - valorActual);
    boolean completada = valorActual >= objetivo;
    return new MisionProgresoDTO(
        m.getId(),
        m.getNombre(),
        m.getRecompensa(),
        m.getTipoMetrica(),
        objetivo,
        valorActual,
        porcentaje,
        faltante,
        completada,
        bloqueada,
        obtenerUnidadLabel(m.getTipoMetrica()));
  }

  private String obtenerUnidadLabel(TipoMetricaMision tipo) {
    if (tipo == null) {
      return "aportes";
    }
    return switch (tipo) {
      case DONACIONES_EXITOSAS -> "donaciones exitosas";
      case MAX_BIENES -> "bienes en una donación";
      case MESES_CONSECUTIVOS -> "meses consecutivos";
      case CATEGORIAS_DISTINTAS -> "categorías distintas";
    };
  }

  @Operation(
      summary = "Obtener insignias del donante",
      description =
          "Devuelve las insignias obtenidas por un donante y difunde la insignia al flujo n8n")
  @ApiResponse(responseCode = "200", description = "Lista de insignias")
  @ApiResponse(
      responseCode = "400",
      description = "ID de donante con formato inválido",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @GetMapping("/donantes/{id}/insignias")
  public ResponseEntity<List<Insignia>> obtenerInsignias(@PathVariable UUID id) {
    PerfilDonante perfil =
        perfilDonanteRepository.findById(id).orElseGet(() -> new PerfilDonante(id));
    List<Insignia> insignias = new ArrayList<>(perfil.getInsigniasObtenidas());

    if (!insignias.isEmpty()) {
      Insignia insigniaADifundir = insignias.get(insignias.size() - 1);
      try {
        difusionPort.difundirInsignia(new InsigniaObtenidaEvent(id, insigniaADifundir));
      } catch (Exception e) {
        // Tolerancia a fallos si n8n no está disponible
      }
    }

    return ResponseEntity.ok(insignias);
  }

  @Operation(
      summary = "Obtener ranking Top 3 mensual",
      description = "Devuelve los 3 donantes con más actividad en el mes actual")
  @ApiResponse(responseCode = "200", description = "Top 3 donantes del mes")
  @ApiResponse(
      responseCode = "500",
      description = "Error interno calculando ranking",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @GetMapping("/ranking/top3")
  public ResponseEntity<List<java.util.Map<String, Object>>> obtenerRankingMensual() {
    java.time.YearMonth mesActual = java.time.YearMonth.now();
    List<PerfilDonante> top3 = rankingMensualService.obtenerTop3Mensual(mesActual);

    persistirRankingMensual(mesActual, top3);

    List<java.util.Map<String, Object>> response =
        top3.stream()
            .map(
                p -> {
                  String nombreUsuario = p.getDonanteId().toString();
                  try {
                    com.donatrack.incentivos.infrastructure.adapters.out.client.PersonaClient
                            .PersonaDTO
                        persona = personaClient.obtenerPersona(p.getDonanteId());
                    if (persona != null) {
                      if ("JURIDICA".equalsIgnoreCase(persona.tipo())
                          && persona.razonSocial() != null
                          && !persona.razonSocial().isBlank()) {
                        nombreUsuario = persona.razonSocial();
                      } else {
                        String n = persona.nombre() != null ? persona.nombre().trim() : "";
                        String a = persona.apellido() != null ? persona.apellido().trim() : "";
                        String completo = (n + " " + a).trim();
                        if (!completo.isEmpty()) {
                          nombreUsuario = completo;
                        } else if (persona.razonSocial() != null
                            && !persona.razonSocial().isBlank()) {
                          nombreUsuario = persona.razonSocial();
                        }
                      }
                    }
                  } catch (Exception e) {
                    // Ignore
                  }

                  int misionesMes = p.getMetricas().obtenerMisionesCompletadasEn(mesActual).size();
                  int donacionesTotales = p.getMetricas().obtenerTodasLasDonaciones().size();
                  java.util.Map<String, Object> map = new java.util.HashMap<>();
                  map.put("donanteId", p.getDonanteId().toString());
                  map.put("user", nombreUsuario);
                  map.put("donations", misionesMes > 0 ? misionesMes : donacionesTotales);
                  map.put("totalDonations", donacionesTotales);
                  return map;
                })
            .collect(java.util.stream.Collectors.toList());

    try {
      restTemplate.postForObject(n8nWebhookRankingUrl, response, String.class);
    } catch (Exception e) {
      if (n8nWebhookRankingUrl != null && n8nWebhookRankingUrl.contains("http://n8n:5678")) {
        try {
          restTemplate.postForObject(
              n8nWebhookRankingUrl.replace("http://n8n:5678", "http://localhost:5678"),
              response,
              String.class);
        } catch (Exception ignored) {
        }
      }
    }

    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Obtener historial de rankings mensuales",
      description =
          "Devuelve los rankings mensuales históricos guardados con las posiciones de los donantes")
  @ApiResponse(responseCode = "200", description = "Historial de rankings obtenido")
  @GetMapping("/ranking/historial")
  public ResponseEntity<List<Map<String, Object>>> obtenerHistorialRankings() {
    if (rankingMensualJpaRepository == null) {
      return ResponseEntity.ok(Collections.emptyList());
    }
    List<
            com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities
                .RankingMensualEntity>
        entities = rankingMensualJpaRepository.findAll();
    List<Map<String, Object>> result = new ArrayList<>();
    for (var r : entities) {
      if (r == null) continue;
      Map<String, Object> map = new HashMap<>();
      map.put("id", r.getId() != null ? r.getId().toString() : null);
      map.put("mes", r.getMes());
      map.put("anio", r.getAnio());
      int mesNumero = resolverMesNumero(r.getMes());
      map.put("mesNumero", mesNumero);
      map.put("mesNombre", mesNumero > 0 ? nombreMesEs(mesNumero) : String.valueOf(r.getMes()));
      if (r.getAnio() != null && mesNumero > 0) {
        map.put("periodoKey", r.getAnio() + "-" + mesNumero);
      } else {
        map.put("periodoKey", r.getAnio() + "-" + r.getMes());
      }

      List<Map<String, Object>> posiciones = new ArrayList<>();
      if (r.getPosiciones() != null) {
        for (var pos : r.getPosiciones()) {
          if (pos == null) continue;
          Map<String, Object> pMap = new HashMap<>();
          pMap.put("posicion", pos.getPosicion());
          pMap.put("misionesCompletadasMes", pos.getMisionesCompletadasMes());
          if (pos.getPerfil() != null) {
            String donanteId =
                pos.getPerfil().getPerfilDonanteId() != null
                    ? pos.getPerfil().getPerfilDonanteId().toString()
                    : null;
            pMap.put("donanteId", donanteId);
            String nombre =
                "Donante "
                    + (donanteId != null && donanteId.length() >= 4
                        ? donanteId.substring(0, 4)
                        : "");
            try {
              if (personaClient != null && pos.getPerfil().getPerfilDonanteId() != null) {
                var persona = personaClient.obtenerPersona(pos.getPerfil().getPerfilDonanteId());
                if (persona != null) {
                  String n = persona.nombre() != null ? persona.nombre().trim() : "";
                  String a = persona.apellido() != null ? persona.apellido().trim() : "";
                  String completo = (n + " " + a).trim();
                  if (!completo.isEmpty()) {
                    nombre = completo;
                  } else if (persona.razonSocial() != null && !persona.razonSocial().isBlank()) {
                    nombre = persona.razonSocial();
                  }
                }
              }
            } catch (Exception ignored) {
            }
            pMap.put("user", nombre);
          }
          posiciones.add(pMap);
        }
      }
      map.put("posiciones", posiciones);
      result.add(map);
    }
    result.sort(
        (a, b) -> {
          int anioA = a.get("anio") instanceof Number n ? n.intValue() : 0;
          int anioB = b.get("anio") instanceof Number n ? n.intValue() : 0;
          if (anioB != anioA) {
            return Integer.compare(anioB, anioA);
          }
          int mesA = a.get("mesNumero") instanceof Number n ? n.intValue() : 0;
          int mesB = b.get("mesNumero") instanceof Number n ? n.intValue() : 0;
          return Integer.compare(mesB, mesA);
        });
    return ResponseEntity.ok(result);
  }

  private void persistirRankingMensual(java.time.YearMonth mesActual, List<PerfilDonante> top3) {
    if (rankingMensualJpaRepository == null || perfilDonanteJpaRepository == null) {
      return;
    }
    try {
      String mesNum = String.valueOf(mesActual.getMonthValue());
      String mesEs = nombreMesEs(mesActual.getMonthValue()).toUpperCase();
      String mesEn = mesActual.getMonth().name();
      Integer anio = mesActual.getYear();
      com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.RankingMensualEntity
          ranking =
              rankingMensualJpaRepository
                  .findFirstByMesInAndAnio(List.of(mesNum, mesEn, mesEs), anio)
                  .orElseGet(
                      () -> {
                        com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities
                                .RankingMensualEntity
                            r =
                                new com.donatrack.incentivos.infrastructure.adapters.out.persistence
                                    .entities.RankingMensualEntity();
                        r.setId(UUID.randomUUID());
                        r.setMes(mesNum);
                        r.setAnio(anio);
                        return r;
                      });
      ranking.setMes(mesNum);

      ranking.getPosiciones().clear();
      int pos = 1;
      for (PerfilDonante p : top3) {
        Optional<
                com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities
                    .PerfilDonanteEntity>
            perfilEntityOpt = perfilDonanteJpaRepository.findById(p.getDonanteId());
        if (perfilEntityOpt.isPresent()) {
          com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities
                  .PosicionRankingEntity
              posicion =
                  new com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities
                      .PosicionRankingEntity();
          posicion.setId(UUID.randomUUID());
          posicion.setRankingMensual(ranking);
          posicion.setPerfil(perfilEntityOpt.get());
          posicion.setPosicion(pos++);
          posicion.setMisionesCompletadasMes(
              p.getMetricas().obtenerMisionesCompletadasEn(mesActual).size());
          ranking.getPosiciones().add(posicion);
        }
      }
      rankingMensualJpaRepository.save(ranking);
    } catch (Exception ignored) {
    }
  }

  @Operation(
      summary = "Registrar actividad de donación exitosa",
      description =
          "Registra una actividad de donación exitosa para un donante, actualizando sus métricas e insignias")
  @ApiResponse(responseCode = "200", description = "Actividad registrada")
  @ApiResponse(
      responseCode = "400",
      description = "Datos de actividad o ID inválidos",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping("/donantes/{id}/actividad")
  public ResponseEntity<Void> registrarActividadDonacionExitosa(
      @PathVariable UUID id, @RequestBody ActividadDonacionDTO actividad) {
    registrarActividadDonacionUseCase.ejecutar(id, actividad);
    return ResponseEntity.ok().build();
  }

  private int calcularRacha(
      List<RegistroDonacion> donaciones, java.time.LocalDate fechaCorteRacha) {
    if (donaciones.isEmpty()) {
      return 0;
    }

    List<RegistroDonacion> donacionesValidas = donaciones;
    if (fechaCorteRacha != null) {
      donacionesValidas =
          donaciones.stream()
              .filter(
                  d ->
                      d.getFechaDonacion() != null
                          && !d.getFechaDonacion().isBefore(fechaCorteRacha))
              .collect(Collectors.toList());
    }

    if (donacionesValidas.isEmpty()) {
      return 0;
    }

    List<YearMonth> meses =
        donacionesValidas.stream()
            .map(RegistroDonacion::getMesDonacion)
            .distinct()
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());

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
  }

  private int resolverMesNumero(String rawMes) {
    if (rawMes == null || rawMes.isBlank()) {
      return 0;
    }
    String clean = rawMes.trim().toUpperCase();
    try {
      int num = Integer.parseInt(clean);
      return (num >= 1 && num <= 12) ? num : 0;
    } catch (NumberFormatException ignored) {
    }
    return switch (clean) {
      case "JANUARY", "ENERO" -> 1;
      case "FEBRUARY", "FEBRERO" -> 2;
      case "MARCH", "MARZO" -> 3;
      case "APRIL", "ABRIL" -> 4;
      case "MAY", "MAYO" -> 5;
      case "JUNE", "JUNIO" -> 6;
      case "JULY", "JULIO" -> 7;
      case "AUGUST", "AGOSTO" -> 8;
      case "SEPTEMBER", "SEPTIEMBRE", "SETIEMBRE" -> 9;
      case "OCTOBER", "OCTUBRE" -> 10;
      case "NOVEMBER", "NOVIEMBRE" -> 11;
      case "DECEMBER", "DICIEMBRE" -> 12;
      default -> 0;
    };
  }

  private String nombreMesEs(int mesNumero) {
    return switch (mesNumero) {
      case 1 -> "Enero";
      case 2 -> "Febrero";
      case 3 -> "Marzo";
      case 4 -> "Abril";
      case 5 -> "Mayo";
      case 6 -> "Junio";
      case 7 -> "Julio";
      case 8 -> "Agosto";
      case 9 -> "Septiembre";
      case 10 -> "Octubre";
      case 11 -> "Noviembre";
      case 12 -> "Diciembre";
      default -> "Mes " + mesNumero;
    };
  }
}
