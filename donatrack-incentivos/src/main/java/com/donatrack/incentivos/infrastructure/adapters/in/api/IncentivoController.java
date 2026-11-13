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
  public ResponseEntity<List<Mision>> obtenerMisionesDisponibles(@PathVariable UUID id) {
    PerfilDonante perfil =
        perfilDonanteRepository.findById(id).orElseGet(() -> new PerfilDonante(id));
    List<Mision> respuesta =
        perfil.getMisionActual() != null
            ? java.util.List.of(perfil.getMisionActual())
            : new java.util.ArrayList<>();
    return ResponseEntity.ok(respuesta);
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
    List<java.util.Map<String, Object>> response = calcularRankingTop3();
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Publicar ranking mensual a canales de difusión (Discord vía n8n)",
      description =
          "Calcula el Top 3 mensual y lo despacha al webhook de automatización de n8n para su difusión en Discord")
  @ApiResponse(responseCode = "200", description = "Ranking publicado exitosamente")
  @ApiResponse(
      responseCode = "500",
      description = "Error publicando ranking",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping("/ranking/publicar")
  public ResponseEntity<java.util.Map<String, Object>> publicarRankingMensual() {
    List<java.util.Map<String, Object>> ranking = calcularRankingTop3();
    boolean notificado = enviarRankingAN8n(ranking);

    java.util.Map<String, Object> result = new java.util.HashMap<>();
    result.put(
        "mensaje",
        notificado
            ? "Ranking publicado exitosamente a Discord vía n8n"
            : "Ranking calculado pero no se pudo contactar a n8n");
    result.put("notificado", notificado);
    result.put("ranking", ranking);
    return ResponseEntity.ok(result);
  }

  private List<java.util.Map<String, Object>> calcularRankingTop3() {
    java.time.YearMonth mesActual = java.time.YearMonth.now();
    List<PerfilDonante> top3 = rankingMensualService.obtenerTop3Mensual(mesActual);

    persistirRankingMensual(mesActual, top3);

    return top3.stream()
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
  }

  private boolean enviarRankingAN8n(List<java.util.Map<String, Object>> ranking) {
    try {
      restTemplate.postForObject(n8nWebhookRankingUrl, ranking, String.class);
      return true;
    } catch (Exception e) {
      if (n8nWebhookRankingUrl != null && n8nWebhookRankingUrl.contains("http://n8n:5678")) {
        try {
          restTemplate.postForObject(
              n8nWebhookRankingUrl.replace("http://n8n:5678", "http://localhost:5678"),
              ranking,
              String.class);
          return true;
        } catch (Exception ignored) {
        }
      }
      return false;
    }
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
    return ResponseEntity.ok(result);
  }

  private void persistirRankingMensual(java.time.YearMonth mesActual, List<PerfilDonante> top3) {
    if (rankingMensualJpaRepository == null || perfilDonanteJpaRepository == null) {
      return;
    }
    try {
      String mes = mesActual.getMonth().name();
      Integer anio = mesActual.getYear();
      com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.RankingMensualEntity
          ranking =
              rankingMensualJpaRepository
                  .findFirstByMesAndAnio(mes, anio)
                  .orElseGet(
                      () -> {
                        com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities
                                .RankingMensualEntity
                            r =
                                new com.donatrack.incentivos.infrastructure.adapters.out.persistence
                                    .entities.RankingMensualEntity();
                        r.setId(UUID.randomUUID());
                        r.setMes(mes);
                        r.setAnio(anio);
                        return r;
                      });

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
}
