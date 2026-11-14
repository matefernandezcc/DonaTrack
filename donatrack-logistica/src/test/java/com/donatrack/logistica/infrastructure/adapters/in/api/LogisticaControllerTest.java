package com.donatrack.logistica.infrastructure.adapters.in.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.donatrack.logistica.application.ports.in.IniciarRutaUseCase;
import com.donatrack.logistica.application.ports.in.ListarItemsPendientesPort;
import com.donatrack.logistica.application.ports.in.ProcesarCallbackPlanificacionUseCase;
import com.donatrack.logistica.application.ports.in.ProcesarPlanificacionesPendientesUseCase;
import com.donatrack.logistica.application.ports.in.RecepcionarDonacionListaPort;
import com.donatrack.logistica.application.ports.out.CamionRepositoryPort;
import com.donatrack.logistica.application.ports.out.ChoferRepositoryPort;
import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepositoryPort;
import com.donatrack.logistica.application.ports.out.StoragePort;
import com.donatrack.logistica.application.usecases.ConfirmarRecepcionUseCase;
import com.donatrack.logistica.application.usecases.ReportarFallaEntregaUseCase;
import com.donatrack.logistica.domain.entities.reparto.Camion;
import com.donatrack.logistica.domain.entities.reparto.Chofer;
import com.donatrack.logistica.domain.entities.reparto.RutaDeReparto;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@DisplayName("LogisticaController — Pruebas de Reasignación Segura y Gestión de Flota")
class LogisticaControllerTest {

  private ListarItemsPendientesPort listarItemsPendientesPort;
  private CamionRepositoryPort camionRepository;
  private ChoferRepositoryPort choferRepository;
  private RutaDeRepartoRepositoryPort rutaRepository;
  private IniciarRutaUseCase iniciarRutaUseCase;
  private ConfirmarRecepcionUseCase confirmarRecepcionUseCase;
  private ReportarFallaEntregaUseCase reportarFallaEntregaUseCase;
  private ProcesarPlanificacionesPendientesUseCase planificacionUseCase;
  private ProcesarCallbackPlanificacionUseCase procesarCallbackPlanificacionUseCase;
  private RecepcionarDonacionListaPort recepcionarDonacionListaPort;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    listarItemsPendientesPort = mock(ListarItemsPendientesPort.class);
    camionRepository = mock(CamionRepositoryPort.class);
    choferRepository = mock(ChoferRepositoryPort.class);
    rutaRepository = mock(RutaDeRepartoRepositoryPort.class);
    iniciarRutaUseCase = mock(IniciarRutaUseCase.class);
    confirmarRecepcionUseCase = mock(ConfirmarRecepcionUseCase.class);
    reportarFallaEntregaUseCase = mock(ReportarFallaEntregaUseCase.class);
    planificacionUseCase = mock(ProcesarPlanificacionesPendientesUseCase.class);
    procesarCallbackPlanificacionUseCase = mock(ProcesarCallbackPlanificacionUseCase.class);
    recepcionarDonacionListaPort = mock(RecepcionarDonacionListaPort.class);

    LogisticaController controller =
        new LogisticaController(
            listarItemsPendientesPort,
            camionRepository,
            choferRepository,
            rutaRepository,
            iniciarRutaUseCase,
            confirmarRecepcionUseCase,
            reportarFallaEntregaUseCase,
            planificacionUseCase,
            procesarCallbackPlanificacionUseCase,
            recepcionarDonacionListaPort);

    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Nested
  @DisplayName("Reasignación al eliminar Camión (DELETE /api/camiones/{patente})")
  class EliminarCamionTests {

    @Test
    @DisplayName("Con rutas asignadas y camión sustituto disponible: reasigna rutas al sustituto y elimina camión")
    void eliminarCamion_conRutasAsignadasYCamionSustituto_reasignaRutasASustitutoYGuarda() throws Exception {
      // Arrange
      String patenteAEliminar = "AA111AA";
      String patenteSustituto = "BB222BB";

      Camion camionAEliminar = new Camion(UUID.randomUUID(), patenteAEliminar, 50.0, 2.5, 3000.0);
      Camion camionSustituto = new Camion(UUID.randomUUID(), patenteSustituto, 60.0, 3.0, 4500.0);

      RutaDeReparto ruta1 = new RutaDeReparto(UUID.randomUUID(), LocalDate.now(), false, camionAEliminar, null, new ArrayList<>());
      RutaDeReparto ruta2 = new RutaDeReparto(UUID.randomUUID(), LocalDate.now(), false, camionAEliminar, null, new ArrayList<>());

      when(rutaRepository.obtenerTodas()).thenReturn(List.of(ruta1, ruta2));
      when(camionRepository.obtenerTodos()).thenReturn(List.of(camionAEliminar, camionSustituto));

      // Act
      mockMvc
          .perform(delete("/api/camiones/" + patenteAEliminar))
          .andExpect(status().isOk());

      // Assert
      assertEquals(camionSustituto, ruta1.getCamion(), "La ruta 1 debe haber sido reasignada al camión sustituto");
      assertEquals(camionSustituto, ruta2.getCamion(), "La ruta 2 debe haber sido reasignada al camión sustituto");
      verify(rutaRepository, times(1)).guardar(ruta1);
      verify(rutaRepository, times(1)).guardar(ruta2);
      verify(camionRepository, times(1)).eliminar(patenteAEliminar);
    }

    @Test
    @DisplayName("Con rutas asignadas y sin camión sustituto: camión de la ruta queda en null de forma segura")
    void eliminarCamion_conRutasAsignadasYSinCamionSustituto_dejaCamionEnNullDeFormaSegura() throws Exception {
      // Arrange
      String patenteAEliminar = "AA111AA";
      Camion camionAEliminar = new Camion(UUID.randomUUID(), patenteAEliminar, 50.0, 2.5, 3000.0);

      RutaDeReparto ruta = new RutaDeReparto(UUID.randomUUID(), LocalDate.now(), false, camionAEliminar, null, new ArrayList<>());

      when(rutaRepository.obtenerTodas()).thenReturn(List.of(ruta));
      when(camionRepository.obtenerTodos()).thenReturn(List.of(camionAEliminar)); // Es el único en la flota

      // Act
      mockMvc
          .perform(delete("/api/camiones/" + patenteAEliminar))
          .andExpect(status().isOk());

      // Assert
      assertNull(ruta.getCamion(), "El camión de la ruta debe quedar en null de forma segura");
      verify(rutaRepository, times(1)).guardar(ruta);
      verify(camionRepository, times(1)).eliminar(patenteAEliminar);
    }

    @Test
    @DisplayName("Sin rutas asignadas: elimina camión sin modificar ninguna ruta")
    void eliminarCamion_sinRutasAsignadas_eliminaCamionSinTocarRutas() throws Exception {
      // Arrange
      String patenteAEliminar = "AA111AA";
      Camion otroCamion = new Camion(UUID.randomUUID(), "OTRO999", 50.0, 2.5, 3000.0);
      RutaDeReparto rutaOtras = new RutaDeReparto(UUID.randomUUID(), LocalDate.now(), false, otroCamion, null, new ArrayList<>());

      when(rutaRepository.obtenerTodas()).thenReturn(List.of(rutaOtras));

      // Act
      mockMvc
          .perform(delete("/api/camiones/" + patenteAEliminar))
          .andExpect(status().isOk());

      // Assert
      verify(rutaRepository, never()).guardar(any());
      verify(camionRepository, times(1)).eliminar(patenteAEliminar);
    }
  }

  @Nested
  @DisplayName("Reasignación al eliminar Chofer (DELETE /api/choferes/{legajo})")
  class EliminarChoferTests {

    @Test
    @DisplayName("Con rutas asignadas y chofer sustituto disponible: reasigna rutas al chofer sustituto y elimina")
    void eliminarChofer_conRutasAsignadasYChoferSustituto_reasignaRutasASustitutoYGuarda() throws Exception {
      // Arrange
      String legajoAEliminar = "CH-001";
      String legajoSustituto = "CH-002";

      Chofer choferAEliminar = new Chofer(UUID.randomUUID(), legajoAEliminar, "Carlos Gómez");
      Chofer choferSustituto = new Chofer(UUID.randomUUID(), legajoSustituto, "Esteban Quito");

      RutaDeReparto ruta1 = new RutaDeReparto(UUID.randomUUID(), LocalDate.now(), false, null, choferAEliminar, new ArrayList<>());
      RutaDeReparto ruta2 = new RutaDeReparto(UUID.randomUUID(), LocalDate.now(), false, null, choferAEliminar, new ArrayList<>());

      when(rutaRepository.obtenerTodas()).thenReturn(List.of(ruta1, ruta2));
      when(choferRepository.obtenerTodos()).thenReturn(List.of(choferAEliminar, choferSustituto));

      // Act
      mockMvc
          .perform(delete("/api/choferes/" + legajoAEliminar))
          .andExpect(status().isOk());

      // Assert
      assertEquals(choferSustituto, ruta1.getChofer(), "La ruta 1 debe haber sido reasignada al chofer sustituto");
      assertEquals(choferSustituto, ruta2.getChofer(), "La ruta 2 debe haber sido reasignada al chofer sustituto");
      verify(rutaRepository, times(1)).guardar(ruta1);
      verify(rutaRepository, times(1)).guardar(ruta2);
      verify(choferRepository, times(1)).eliminar(legajoAEliminar);
    }

    @Test
    @DisplayName("Con rutas asignadas y sin chofer sustituto: chofer de la ruta queda en null de forma segura")
    void eliminarChofer_conRutasAsignadasYSinChoferSustituto_dejaChoferEnNullDeFormaSegura() throws Exception {
      // Arrange
      String legajoAEliminar = "CH-001";
      Chofer choferAEliminar = new Chofer(UUID.randomUUID(), legajoAEliminar, "Carlos Gómez");

      RutaDeReparto ruta = new RutaDeReparto(UUID.randomUUID(), LocalDate.now(), false, null, choferAEliminar, new ArrayList<>());

      when(rutaRepository.obtenerTodas()).thenReturn(List.of(ruta));
      when(choferRepository.obtenerTodos()).thenReturn(List.of(choferAEliminar)); // Único chofer

      // Act
      mockMvc
          .perform(delete("/api/choferes/" + legajoAEliminar))
          .andExpect(status().isOk());

      // Assert
      assertNull(ruta.getChofer(), "El chofer de la ruta debe quedar en null de forma segura");
      verify(rutaRepository, times(1)).guardar(ruta);
      verify(choferRepository, times(1)).eliminar(legajoAEliminar);
    }

    @Test
    @DisplayName("Sin rutas asignadas: elimina chofer sin modificar ninguna ruta")
    void eliminarChofer_sinRutasAsignadas_eliminaChoferSinTocarRutas() throws Exception {
      // Arrange
      String legajoAEliminar = "CH-001";
      Chofer otroChofer = new Chofer(UUID.randomUUID(), "CH-999", "Otro Chofer");
      RutaDeReparto rutaOtras = new RutaDeReparto(UUID.randomUUID(), LocalDate.now(), false, null, otroChofer, new ArrayList<>());

      when(rutaRepository.obtenerTodas()).thenReturn(List.of(rutaOtras));

      // Act
      mockMvc
          .perform(delete("/api/choferes/" + legajoAEliminar))
          .andExpect(status().isOk());

      // Assert
      verify(rutaRepository, never()).guardar(any());
      verify(choferRepository, times(1)).eliminar(legajoAEliminar);
    }
  }

  @Nested
  @DisplayName("Consultas de Flota")
  class ListarFlotaTests {

    @Test
    @DisplayName("GET /api/camiones retorna lista de camiones registrados")
    void listarCamiones_retornaListaCamiones() throws Exception {
      Camion c1 = new Camion(UUID.randomUUID(), "AA100BB", 30.0, 2.0, 1500.0);
      when(camionRepository.obtenerTodos()).thenReturn(List.of(c1));

      mockMvc
          .perform(get("/api/camiones"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].patente").value("AA100BB"));
    }

    @Test
    @DisplayName("GET /api/choferes retorna lista de choferes registrados")
    void listarChoferes_retornaListaChoferes() throws Exception {
      Chofer ch1 = new Chofer(UUID.randomUUID(), "CH-100", "Mario Rossi");
      when(choferRepository.obtenerTodos()).thenReturn(List.of(ch1));

      mockMvc
          .perform(get("/api/choferes"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].legajo").value("CH-100"))
          .andExpect(jsonPath("$[0].nombre").value("Mario Rossi"));
    }
  }
}
