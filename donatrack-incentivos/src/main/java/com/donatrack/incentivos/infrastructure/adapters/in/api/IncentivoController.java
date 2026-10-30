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

    MetricasDonanteDTO dto =
        MetricasDonanteDTO.builder()
            .donanteId(perfil.getDonanteId())
            .totalDonacionesHistoricas(totalHistorico)
            .rachaDeMeses(racha)
            .cantidadBienesDonados(bienesDonados)
            .donacionesExitosas(totalExitosas)
            .totalOrganizacionesAyudadas((int) organizacionesAyudadas)
            .historialDonacionesPorMes(historialMap)
            .posicionRanking(5) // Mock
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

    if (insignias.isEmpty()) {
      Insignia insigniaDefault =
          new Insignia("Primer Aporte", "Otorgada al realizar la primera donación en el sistema");
      insigniaDefault.setId(UUID.fromString("e1111111-1111-4111-8111-111111111111"));
      insigniaDefault.setFechaObtencion(java.time.LocalDate.now());
      insigniaDefault.setVisiblePublicamente(true);
      insignias.add(insigniaDefault);
      perfil.setInsigniasObtenidas(insignias);
      try {
        perfilDonanteRepository.save(perfil);
      } catch (Exception e) {
        // Tolerancia a fallos en persistencia
      }
    }

    Insignia insigniaADifundir = insignias.get(insignias.size() - 1);
    try {
      difusionPort.difundirInsignia(new InsigniaObtenidaEvent(id, insigniaADifundir));
    } catch (Exception e) {
      // Tolerancia a fallos si n8n no está disponible
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
                      if ("JURIDICA".equals(persona.tipo())) {
                        nombreUsuario = persona.razonSocial();
                      } else {
                        nombreUsuario = persona.nombre() + " " + persona.apellido();
                      }
                    }
                  } catch (Exception e) {
                    // Ignore
                  }

                  int misionesMes = p.getMetricas().obtenerMisionesCompletadasEn(mesActual).size();
                  int donacionesTotales = p.getMetricas().obtenerTodasLasDonaciones().size();
                  java.util.Map<String, Object> map = new java.util.HashMap<>();
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
