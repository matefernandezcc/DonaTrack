package com.donatrack.incentivos.infrastructure.adapters.in.api;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.donatrack.common.dto.ActividadDonacionDTO;
import com.donatrack.incentivos.application.ports.out.DifusionPort;
import com.donatrack.incentivos.application.ports.out.PerfilDonanteRepository;
import com.donatrack.incentivos.application.usecases.RegistrarActividadDonacionUseCase;
import com.donatrack.incentivos.domain.entities.PerfilDonante;
import com.donatrack.incentivos.domain.services.RankingMensualService;
import com.donatrack.incentivos.infrastructure.adapters.out.client.PersonaClient;
import com.donatrack.incentivos.infrastructure.adapters.out.client.PersonaClient.PersonaDTO;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.PerfilDonanteEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.PosicionRankingEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.RankingMensualEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.repositories.PerfilDonanteJpaRepository;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.repositories.RankingMensualJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@DisplayName("IncentivoController — Tests Unitarios MockMvc")
class IncentivoControllerTest {

  private MockMvc mockMvc;

  private RankingMensualService rankingMensualService;
  private RegistrarActividadDonacionUseCase registrarActividadDonacionUseCase;
  private PersonaClient personaClient;
  private PerfilDonanteRepository perfilDonanteRepository;
  private DifusionPort difusionPort;
  private RankingMensualJpaRepository rankingMensualJpaRepository;
  private PerfilDonanteJpaRepository perfilDonanteJpaRepository;


  @BeforeEach
  void setUp() {
    rankingMensualService = mock(RankingMensualService.class);
    registrarActividadDonacionUseCase = mock(RegistrarActividadDonacionUseCase.class);
    personaClient = mock(PersonaClient.class);
    perfilDonanteRepository = mock(PerfilDonanteRepository.class);
    difusionPort = mock(DifusionPort.class);
    rankingMensualJpaRepository = mock(RankingMensualJpaRepository.class);
    perfilDonanteJpaRepository = mock(PerfilDonanteJpaRepository.class);

    IncentivoController controller =
        new IncentivoController(
            rankingMensualService,
            registrarActividadDonacionUseCase,
            personaClient,
            perfilDonanteRepository,
            difusionPort,
            rankingMensualJpaRepository,
            perfilDonanteJpaRepository);

    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  // ==========================================
  // HISTORIAL DE RANKINGS MENSUALES
  // ==========================================

  @Nested
  @DisplayName("Endpoint GET /api/ranking/historial")
  class HistorialRankingsTests {

    @Test
    @DisplayName("GET /api/ranking/historial retorna historial completo con datos de personas resueltos")
    void testObtenerHistorialConDatos() throws Exception {
      UUID rankingId = UUID.randomUUID();
      UUID donanteId1 = UUID.fromString("a1111111-1111-4111-8111-111111111111");
      UUID donanteId2 = UUID.fromString("b2222222-2222-4222-8222-222222222222");

      PerfilDonanteEntity perfil1 = new PerfilDonanteEntity();
      perfil1.setPerfilDonanteId(donanteId1);

      PerfilDonanteEntity perfil2 = new PerfilDonanteEntity();
      perfil2.setPerfilDonanteId(donanteId2);

      RankingMensualEntity rankingEntity = new RankingMensualEntity();
      rankingEntity.setId(rankingId);
      rankingEntity.setMes("NOVIEMBRE");
      rankingEntity.setAnio(2026);

      PosicionRankingEntity pos1 = new PosicionRankingEntity();
      pos1.setId(UUID.randomUUID());
      pos1.setPosicion(1);
      pos1.setMisionesCompletadasMes(5);
      pos1.setPerfil(perfil1);
      pos1.setRankingMensual(rankingEntity);

      PosicionRankingEntity pos2 = new PosicionRankingEntity();
      pos2.setId(UUID.randomUUID());
      pos2.setPosicion(2);
      pos2.setMisionesCompletadasMes(3);
      pos2.setPerfil(perfil2);
      pos2.setRankingMensual(rankingEntity);

      rankingEntity.setPosiciones(new ArrayList<>(List.of(pos1, pos2)));

      when(rankingMensualJpaRepository.findAll()).thenReturn(List.of(rankingEntity));
      when(personaClient.obtenerPersona(donanteId1))
          .thenReturn(new PersonaDTO("María", "González", null, "HUMANA"));
      when(personaClient.obtenerPersona(donanteId2))
          .thenReturn(new PersonaDTO(null, null, "Empresa Solidaria S.A.", "JURIDICA"));

      mockMvc
          .perform(get("/api/ranking/historial"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(1)))
          .andExpect(jsonPath("$[0].id").value(rankingId.toString()))
          .andExpect(jsonPath("$[0].mes").value("NOVIEMBRE"))
          .andExpect(jsonPath("$[0].anio").value(2026))
          .andExpect(jsonPath("$[0].posiciones", hasSize(2)))
          .andExpect(jsonPath("$[0].posiciones[0].posicion").value(1))
          .andExpect(jsonPath("$[0].posiciones[0].misionesCompletadasMes").value(5))
          .andExpect(jsonPath("$[0].posiciones[0].donanteId").value(donanteId1.toString()))
          .andExpect(jsonPath("$[0].posiciones[0].user").value("María González"))
          .andExpect(jsonPath("$[0].posiciones[1].posicion").value(2))
          .andExpect(jsonPath("$[0].posiciones[1].user").value("Empresa Solidaria S.A."));

      verify(rankingMensualJpaRepository, times(1)).findAll();
      verify(personaClient, times(1)).obtenerPersona(donanteId1);
      verify(personaClient, times(1)).obtenerPersona(donanteId2);
    }

    @Test
    @DisplayName("GET /api/ranking/historial maneja fallback cuando personaClient falla o retorna null")
    void testObtenerHistorialFallbackPersonaClient() throws Exception {
      UUID rankingId = UUID.randomUUID();
      UUID donanteId = UUID.fromString("c3333333-3333-4333-8333-333333333333");

      PerfilDonanteEntity perfil = new PerfilDonanteEntity();
      perfil.setPerfilDonanteId(donanteId);

      RankingMensualEntity rankingEntity = new RankingMensualEntity();
      rankingEntity.setId(rankingId);
      rankingEntity.setMes("OCTUBRE");
      rankingEntity.setAnio(2026);

      PosicionRankingEntity pos = new PosicionRankingEntity();
      pos.setId(UUID.randomUUID());
      pos.setPosicion(1);
      pos.setMisionesCompletadasMes(2);
      pos.setPerfil(perfil);
      pos.setRankingMensual(rankingEntity);

      rankingEntity.setPosiciones(new ArrayList<>(List.of(pos)));

      when(rankingMensualJpaRepository.findAll()).thenReturn(List.of(rankingEntity));
      when(personaClient.obtenerPersona(donanteId)).thenThrow(new RuntimeException("Servicio de personas caído"));

      mockMvc
          .perform(get("/api/ranking/historial"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].posiciones[0].user").value("Donante c333"));
    }

    @Test
    @DisplayName("GET /api/ranking/historial cuando no hay rankings guardados retorna lista vacía")
    void testObtenerHistorialVacio() throws Exception {
      when(rankingMensualJpaRepository.findAll()).thenReturn(Collections.emptyList());

      mockMvc
          .perform(get("/api/ranking/historial"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/ranking/historial cuando rankingMensualJpaRepository es null retorna lista vacía")
    void testObtenerHistorialRepositoryNull() throws Exception {
      IncentivoController controllerSinRepo =
          new IncentivoController(
              rankingMensualService,
              registrarActividadDonacionUseCase,
              personaClient,
              perfilDonanteRepository,
              difusionPort);

      MockMvc mockMvcSinRepo = MockMvcBuilders.standaloneSetup(controllerSinRepo).build();

      mockMvcSinRepo
          .perform(get("/api/ranking/historial"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(0)));
    }
  }

  // ==========================================
  // MÉTRICAS DEL DONANTE
  // ==========================================

  @Nested
  @DisplayName("Endpoint GET /api/donantes/{id}/metricas")
  class MetricasDonanteTests {

    @Test
    @DisplayName("GET /api/donantes/{id}/metricas retorna métricas calculadas del donante")
    void testObtenerMetricasDonanteExistente() throws Exception {
      UUID donanteId = UUID.randomUUID();
      PerfilDonante perfil = new PerfilDonante(donanteId);
      when(perfilDonanteRepository.findById(donanteId)).thenReturn(Optional.of(perfil));

      mockMvc
          .perform(get("/api/donantes/{id}/metricas", donanteId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.donanteId").value(donanteId.toString()))
          .andExpect(jsonPath("$.totalDonacionesHistoricas").value(0))
          .andExpect(jsonPath("$.puntosTotales").value(0));
    }

    @Test
    @DisplayName("GET /api/donantes/{id}/metricas inicializa perfil por defecto si no existe")
    void testObtenerMetricasDonanteNuevo() throws Exception {
      UUID donanteId = UUID.randomUUID();
      when(perfilDonanteRepository.findById(donanteId)).thenReturn(Optional.empty());

      mockMvc
          .perform(get("/api/donantes/{id}/metricas", donanteId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.donanteId").value(donanteId.toString()))
          .andExpect(jsonPath("$.totalDonacionesHistoricas").value(0));
    }
  }

  // ==========================================
  // MISIONES E INSIGNIAS
  // ==========================================

  @Nested
  @DisplayName("Endpoints de Misiones e Insignias")
  class MisionesInsigniasTests {

    @Test
    @DisplayName("GET /api/donantes/{id}/misiones retorna lista de misiones activas")
    void testObtenerMisiones() throws Exception {
      UUID donanteId = UUID.randomUUID();
      PerfilDonante perfil = new PerfilDonante(donanteId);
      when(perfilDonanteRepository.findById(donanteId)).thenReturn(Optional.of(perfil));

      mockMvc
          .perform(get("/api/donantes/{id}/misiones", donanteId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/donantes/{id}/insignias retorna insignias obtenidas")
    void testObtenerInsignias() throws Exception {
      UUID donanteId = UUID.randomUUID();
      PerfilDonante perfil = new PerfilDonante(donanteId);
      when(perfilDonanteRepository.findById(donanteId)).thenReturn(Optional.of(perfil));

      mockMvc
          .perform(get("/api/donantes/{id}/insignias", donanteId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray());
    }
  }

  // ==========================================
  // RANKING TOP 3 Y ACTIVIDAD
  // ==========================================

  @Nested
  @DisplayName("Top 3 Mensual y Registro de Actividad")
  class RankingTop3YActividadTests {

    @Test
    @DisplayName("GET /api/ranking/top3 retorna lista top 3 calculada por el servicio")
    void testObtenerRankingTop3() throws Exception {
      UUID donanteId = UUID.randomUUID();
      PerfilDonante p1 = new PerfilDonante(donanteId);
      when(rankingMensualService.obtenerTop3Mensual(any(YearMonth.class)))
          .thenReturn(List.of(p1));

      mockMvc
          .perform(get("/api/ranking/top3"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(1)))
          .andExpect(jsonPath("$[0].donanteId").value(donanteId.toString()));
    }

    @Test
    @DisplayName("POST /api/donantes/{id}/actividad invoca use case y devuelve 200 OK")
    void testRegistrarActividadExitosa() throws Exception {
      UUID donanteId = UUID.randomUUID();
      UUID donacionId = UUID.randomUUID();
      UUID benefId = UUID.randomUUID();
      String json =
          String.format(
              """
              {
                "idDonacion": "%s",
                "idDonante": "%s",
                "cantidadBienes": 10,
                "categorias": ["ALIMENTOS"],
                "idEntidadBeneficiaria": "%s",
                "fecha": "2026-11-09"
              }
              """,
              donacionId, donanteId, benefId);

      mockMvc
          .perform(
              post("/api/donantes/{id}/actividad", donanteId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(json))
          .andExpect(status().isOk());

      verify(registrarActividadDonacionUseCase, times(1))
          .ejecutar(eq(donanteId), any(ActividadDonacionDTO.class));
    }
  }
}
