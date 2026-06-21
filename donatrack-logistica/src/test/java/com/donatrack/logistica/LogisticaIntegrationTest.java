package com.donatrack.logistica;

import com.donatrack.logistica.application.ports.in.ConfirmarEntregaRequest;
import com.donatrack.logistica.application.ports.in.PlanificarRutasRequest;
import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepository;
import com.donatrack.logistica.application.ports.out.SolicitudPlanificacionRepository;
import com.donatrack.logistica.domain.entities.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = LogisticaApplication.class)
@AutoConfigureMockMvc
public class LogisticaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SolicitudPlanificacionRepository solicitudRepository;

    @Autowired
    private RutaDeRepartoRepository rutaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID donacionId;

    @BeforeEach
    public void setUp() {
        donacionId = UUID.randomUUID();
    }

    @Test
    public void testFlujoCompletoLogistica() throws Exception {
        // 1. Trigger de Planificación
        PlanificarRutasRequest planificarRequest = new PlanificarRutasRequest(Collections.singletonList(donacionId));
        
        String resultJson = mockMvc.perform(post("/api/planificar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planificarRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        SolicitudPlanificacion solicitud = objectMapper.readValue(resultJson, SolicitudPlanificacion.class);
        assertThat(solicitud).isNotNull();
        assertThat(solicitud.getEstado()).isEqualTo(EstadoPlanificacion.PENDIENTE);
        assertThat(solicitud.getIdsDonaciones()).containsExactly(donacionId);

        // 2. Simulación de Callback de forma asíncrona
        // En nuestro adapter externo pusimos un CompletableFuture que después de 1 segundo actualiza la solicitud
        Thread.sleep(1500);

        // Comprobar que la solicitud pasó a PROCESADA
        SolicitudPlanificacion solicitudProcesada = solicitudRepository.buscarPorId(solicitud.getId()).orElse(null);
        assertThat(solicitudProcesada).isNotNull();
        assertThat(solicitudProcesada.getEstado()).isEqualTo(EstadoPlanificacion.PROCESADA);
        assertThat(solicitudProcesada.getRutasGeneradas()).isNotEmpty();

        // Obtener la ruta generada
        RutaDeReparto ruta = solicitudProcesada.getRutasGeneradas().get(0);
        assertThat(ruta).isNotNull();
        assertThat(ruta.getIniciada()).isFalse();

        // 3. Iniciar recorrido de ruta
        mockMvc.perform(put("/api/rutas/" + ruta.getId() + "/iniciar")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        RutaDeReparto rutaIniciada = rutaRepository.buscarPorId(ruta.getId()).orElse(null);
        assertThat(rutaIniciada).isNotNull();
        assertThat(rutaIniciada.getIniciada()).isTrue();

        // Comprobar que la entrega pasó a EN_TRASLADO
        Entrega entrega = rutaIniciada.getParadas().get(0).getEntregas().get(0);
        assertThat(entrega.getEstado()).isEqualTo(EstadoEntrega.EN_TRASLADO);

        // 4. Confirmar entrega por la entidad
        ConfirmarEntregaRequest confirmarRequest = new ConfirmarEntregaRequest(
                List.of("http://foto1.com", "http://foto2.com"),
                "AAA-123"
        );
        mockMvc.perform(put("/api/entregas/" + donacionId + "/confirmar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmarRequest)))
                .andExpect(status().isOk());

        RutaDeReparto rutaFinal = rutaRepository.buscarPorId(ruta.getId()).orElse(null);
        Entrega entregaConfirmada = rutaFinal.getParadas().get(0).getEntregas().get(0);
        assertThat(entregaConfirmada.getEstado()).isEqualTo(EstadoEntrega.ENTREGADA);
        assertThat(entregaConfirmada.getComprobanteRecepcion()).isNotNull();
        assertThat(entregaConfirmada.getComprobanteRecepcion().getFotos()).hasSize(2);
    }
}
