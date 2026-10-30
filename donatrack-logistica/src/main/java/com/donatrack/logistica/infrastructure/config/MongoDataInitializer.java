package com.donatrack.logistica.infrastructure.config;

import com.donatrack.logistica.application.ports.out.CamionRepositoryPort;
import com.donatrack.logistica.application.ports.out.ChoferRepositoryPort;
import com.donatrack.logistica.application.ports.out.EntregaRepositoryPort;
import com.donatrack.logistica.application.ports.out.ItemPlanificacionRepositoryPort;
import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepositoryPort;
import com.donatrack.logistica.application.ports.out.SolicitudPlanificacionRepositoryPort;
import com.donatrack.logistica.domain.entities.entregas.ComprobanteRecepcion;
import com.donatrack.logistica.domain.entities.entregas.Entrega;
import com.donatrack.logistica.domain.entities.entregas.EstadoEntrega;
import com.donatrack.logistica.domain.entities.planificacion.EstadoPlanificacion;
import com.donatrack.logistica.domain.entities.planificacion.ItemPlanificacion;
import com.donatrack.logistica.domain.entities.planificacion.SolicitudPlanificacion;
import com.donatrack.logistica.domain.entities.reparto.Camion;
import com.donatrack.logistica.domain.entities.reparto.Chofer;
import com.donatrack.logistica.domain.entities.reparto.Coordenada;
import com.donatrack.logistica.domain.entities.reparto.Direccion;
import com.donatrack.logistica.domain.entities.reparto.Parada;
import com.donatrack.logistica.domain.entities.reparto.RutaDeReparto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Precarga de datos enriquecidos en MongoDB (para entorno local y nube cuando la base esté vacía).
 * Asegura la disponibilidad inmediata de entidades en las 6 colecciones.
 * Excluido en perfil de test para no interferir con tests unitarios e integrados.
 */
@Component
@Profile("!test")
public class MongoDataInitializer implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(MongoDataInitializer.class);

  private final CamionRepositoryPort camionRepository;
  private final ChoferRepositoryPort choferRepository;
  private final ItemPlanificacionRepositoryPort itemPlanificacionRepository;
  private final RutaDeRepartoRepositoryPort rutaRepository;
  private final EntregaRepositoryPort entregaRepository;
  private final SolicitudPlanificacionRepositoryPort solicitudRepository;

  public MongoDataInitializer(
      CamionRepositoryPort camionRepository,
      ChoferRepositoryPort choferRepository,
      ItemPlanificacionRepositoryPort itemPlanificacionRepository,
      RutaDeRepartoRepositoryPort rutaRepository,
      EntregaRepositoryPort entregaRepository,
      SolicitudPlanificacionRepositoryPort solicitudRepository) {
    this.camionRepository = camionRepository;
    this.choferRepository = choferRepository;
    this.itemPlanificacionRepository = itemPlanificacionRepository;
    this.rutaRepository = rutaRepository;
    this.entregaRepository = entregaRepository;
    this.solicitudRepository = solicitudRepository;
  }

  @Override
  public void run(String... args) {
    if (camionRepository.obtenerTodos().isEmpty()) {
      log.info(">>> [MongoDataInitializer] Base vacía detectada. Precargando las 6 colecciones...");

      // 1. Camiones (8)
      Camion c1 = new Camion(UUID.fromString("c1111111-1111-4111-8111-111111111111"), "AA123BB", 20.0, 3.2, 4000.0);
      Camion c2 = new Camion(UUID.fromString("c2222222-2222-4222-8222-222222222222"), "AF456CD", 12.5, 2.8, 2500.0);
      Camion c3 = new Camion(UUID.fromString("c3333333-3333-4333-8333-333333333333"), "AC789EE", 30.0, 3.8, 8000.0);
      Camion c4 = new Camion(UUID.fromString("c4444444-4444-4444-8444-444444444444"), "AD102ZX", 15.0, 3.0, 3200.0);
      Camion c5 = new Camion(UUID.fromString("c5555555-5555-4555-8555-555555555555"), "AE304KL", 25.0, 3.5, 5500.0);
      Camion c6 = new Camion(UUID.fromString("c6666666-6666-4666-8666-666666666666"), "AG555OP", 10.0, 2.5, 1800.0);
      Camion c7 = new Camion(UUID.fromString("c7777777-7777-4777-8777-777777777777"), "AH890QW", 45.0, 4.0, 12000.0);
      Camion c8 = new Camion(UUID.fromString("c8888888-8888-4888-8888-888888888888"), "AI432MN", 8.0, 2.2, 1200.0);
      List.of(c1, c2, c3, c4, c5, c6, c7, c8).forEach(camionRepository::guardar);
      log.info(">>> [MongoDataInitializer] 8 Camiones precargados.");

      // 2. Choferes (8)
      Chofer f1 = new Chofer(UUID.fromString("f1111111-1111-4111-8111-111111111111"), "CH-101", "Roberto Gómez");
      Chofer f2 = new Chofer(UUID.fromString("f2222222-2222-4222-8222-222222222222"), "CH-102", "Laura Fernández");
      Chofer f3 = new Chofer(UUID.fromString("f3333333-3333-4333-8333-333333333333"), "CH-103", "Carlos Atlas");
      Chofer f4 = new Chofer(UUID.fromString("f4444444-4444-4444-8444-444444444444"), "CH-104", "Martín Benítez");
      Chofer f5 = new Chofer(UUID.fromString("f5555555-5555-4555-8555-555555555555"), "CH-105", "Sofía Morales");
      Chofer f6 = new Chofer(UUID.fromString("f6666666-6666-4666-8666-666666666666"), "CH-106", "Esteban Rossi");
      Chofer f7 = new Chofer(UUID.fromString("f7777777-7777-4777-8777-777777777777"), "CH-107", "Lucía Herrera");
      Chofer f8 = new Chofer(UUID.fromString("f8888888-8888-4888-8888-888888888888"), "CH-108", "Mariano Di Pietro");
      List.of(f1, f2, f3, f4, f5, f6, f7, f8).forEach(choferRepository::guardar);
      log.info(">>> [MongoDataInitializer] 8 Choferes precargados.");

      // 3. Items de Planificación (8)
      itemPlanificacionRepository.guardar(new ItemPlanificacion(UUID.fromString("31008064-071a-4d7a-ac37-33318f7d9841"), 120.0, 0.8, new Direccion("Av. Corrientes", "1234", "CABA")));
      itemPlanificacionRepository.guardar(new ItemPlanificacion(UUID.fromString("32008064-071a-4d7a-ac37-33318f7d9842"), 250.0, 1.5, new Direccion("Calle 50", "742", "La Plata")));
      itemPlanificacionRepository.guardar(new ItemPlanificacion(UUID.fromString("33008064-071a-4d7a-ac37-33318f7d9843"), 85.0, 0.5, new Direccion("Av. Santa Fe", "3200", "Palermo, CABA")));
      itemPlanificacionRepository.guardar(new ItemPlanificacion(UUID.fromString("34008064-071a-4d7a-ac37-33318f7d9844"), 310.0, 2.1, new Direccion("Belgrano", "450", "Quilmes")));
      itemPlanificacionRepository.guardar(new ItemPlanificacion(UUID.fromString("35008064-071a-4d7a-ac37-33318f7d9845"), 175.0, 1.1, new Direccion("Av. Centenario", "1820", "San Isidro")));
      itemPlanificacionRepository.guardar(new ItemPlanificacion(UUID.fromString("36008064-071a-4d7a-ac37-33318f7d9846"), 95.0, 0.6, new Direccion("Defensa", "800", "San Telmo, CABA")));
      itemPlanificacionRepository.guardar(new ItemPlanificacion(UUID.fromString("37008064-071a-4d7a-ac37-33318f7d9847"), 420.0, 3.0, new Direccion("Av. Rivadavia", "8500", "Flores, CABA")));
      itemPlanificacionRepository.guardar(new ItemPlanificacion(UUID.fromString("38008064-071a-4d7a-ac37-33318f7d9848"), 60.0, 0.4, new Direccion("Calle 12", "1430", "Berazategui")));
      log.info(">>> [MongoDataInitializer] 8 Items de Planificación precargados.");

      // 4. Entregas (8)
      entregaRepository.guardar(new Entrega(UUID.fromString("31008064-071a-4d7a-ac37-33318f7d9841"), EstadoEntrega.PENDIENTE, 120.0, 0.8, null));
      entregaRepository.guardar(new Entrega(UUID.fromString("32008064-071a-4d7a-ac37-33318f7d9842"), EstadoEntrega.ENTREGADA, 250.0, 1.5,
          new ComprobanteRecepcion(LocalDateTime.now().minusDays(1), List.of("https://storage.donatrack.org/remitos/remito-lp-742.pdf"), "AC789EE")));
      entregaRepository.guardar(new Entrega(UUID.fromString("33008064-071a-4d7a-ac37-33318f7d9843"), EstadoEntrega.ENTREGADA, 85.0, 0.5,
          new ComprobanteRecepcion(LocalDateTime.now().minusHours(2), List.of("https://storage.donatrack.org/remitos/remito-3300.jpg"), "AF456CD")));
      entregaRepository.guardar(new Entrega(UUID.fromString("34008064-071a-4d7a-ac37-33318f7d9844"), EstadoEntrega.PENDIENTE, 310.0, 2.1, null));
      entregaRepository.guardar(new Entrega(UUID.fromString("35008064-071a-4d7a-ac37-33318f7d9845"), EstadoEntrega.EN_TRASLADO, 175.0, 1.1, null));
      entregaRepository.guardar(new Entrega(UUID.fromString("36008064-071a-4d7a-ac37-33318f7d9846"), EstadoEntrega.PENDIENTE, 95.0, 0.6, null));
      entregaRepository.guardar(new Entrega(UUID.fromString("37008064-071a-4d7a-ac37-33318f7d9847"), EstadoEntrega.NO_RECIBIDA, 420.0, 3.0, null));
      entregaRepository.guardar(new Entrega(UUID.fromString("139e0f9f-9431-4302-9f06-cad97978f7be"), EstadoEntrega.EN_TRASLADO, 50.0, 0.4, null));
      log.info(">>> [MongoDataInitializer] 8 Entregas precargadas.");

      // 5. Solicitudes de Planificación (5)
      solicitudRepository.guardar(new SolicitudPlanificacion(UUID.fromString("b1111111-1111-4111-8111-111111111111"), LocalDateTime.now().minusDays(3), EstadoPlanificacion.PROCESADA, List.of(UUID.fromString("31008064-071a-4d7a-ac37-33318f7d9841"), UUID.fromString("36008064-071a-4d7a-ac37-33318f7d9846"))));
      solicitudRepository.guardar(new SolicitudPlanificacion(UUID.fromString("b2222222-2222-4222-8222-222222222222"), LocalDateTime.now().minusDays(2), EstadoPlanificacion.PROCESADA, List.of(UUID.fromString("32008064-071a-4d7a-ac37-33318f7d9842"))));
      solicitudRepository.guardar(new SolicitudPlanificacion(UUID.fromString("b3333333-3333-4333-8333-333333333333"), LocalDateTime.now().minusDays(1), EstadoPlanificacion.RECIBIDA, List.of(UUID.fromString("33008064-071a-4d7a-ac37-33318f7d9843"), UUID.fromString("35008064-071a-4d7a-ac37-33318f7d9845"))));
      solicitudRepository.guardar(new SolicitudPlanificacion(UUID.fromString("b4444444-4444-4444-8444-444444444444"), LocalDateTime.now().minusHours(5), EstadoPlanificacion.PENDIENTE, List.of(UUID.fromString("34008064-071a-4d7a-ac37-33318f7d9844"), UUID.fromString("37008064-071a-4d7a-ac37-33318f7d9847"))));
      solicitudRepository.guardar(new SolicitudPlanificacion(UUID.fromString("b5555555-5555-4555-8555-555555555555"), LocalDateTime.now().minusHours(1), EstadoPlanificacion.PENDIENTE, List.of(UUID.fromString("38008064-071a-4d7a-ac37-33318f7d9848"), UUID.fromString("139e0f9f-9431-4302-9f06-cad97978f7be"))));
      log.info(">>> [MongoDataInitializer] 5 Solicitudes de Planificación precargadas.");

      // 6. Rutas de Reparto (4)
      List<Parada> paradasRuta1 = List.of(
          new Parada(1, new Direccion("Av. Corrientes", "1234", "CABA"), new Coordenada(-34.6037, -58.3816),
              List.of(new Entrega(UUID.fromString("31008064-071a-4d7a-ac37-33318f7d9841"), EstadoEntrega.PENDIENTE, 120.0, 0.8, null))),
          new Parada(2, new Direccion("Defensa", "800", "San Telmo, CABA"), new Coordenada(-34.6186, -58.3712),
              List.of(new Entrega(UUID.fromString("36008064-071a-4d7a-ac37-33318f7d9846"), EstadoEntrega.PENDIENTE, 95.0, 0.6, null)))
      );
      rutaRepository.guardar(new RutaDeReparto(UUID.fromString("d3b07384-d113-4a11-9a28-97193a0dc951"), LocalDate.now(), false, c1, f1, paradasRuta1));

      List<Parada> paradasRuta2 = List.of(
          new Parada(1, new Direccion("Av. Santa Fe", "3200", "Palermo, CABA"), new Coordenada(-34.5863, -58.4103),
              List.of(new Entrega(UUID.fromString("33008064-071a-4d7a-ac37-33318f7d9843"), EstadoEntrega.ENTREGADA, 85.0, 0.5,
                  new ComprobanteRecepcion(LocalDateTime.now().minusHours(2), List.of("https://storage.donatrack.org/remitos/remito-3300.jpg"), "AF456CD")))),
          new Parada(2, new Direccion("Av. Centenario", "1820", "San Isidro"), new Coordenada(-34.4716, -58.5284),
              List.of(new Entrega(UUID.fromString("35008064-071a-4d7a-ac37-33318f7d9845"), EstadoEntrega.EN_TRASLADO, 175.0, 1.1, null)))
      );
      rutaRepository.guardar(new RutaDeReparto(UUID.fromString("e4c18495-e224-4b22-ab39-08204b1ed062"), LocalDate.now(), true, c2, f2, paradasRuta2));

      List<Parada> paradasRuta3 = List.of(
          new Parada(1, new Direccion("Calle 50", "742", "La Plata"), new Coordenada(-34.9214, -57.9545),
              List.of(new Entrega(UUID.fromString("32008064-071a-4d7a-ac37-33318f7d9842"), EstadoEntrega.ENTREGADA, 250.0, 1.5,
                  new ComprobanteRecepcion(LocalDateTime.now().minusDays(1), List.of("https://storage.donatrack.org/remitos/remito-lp-742.pdf"), "AC789EE"))))
      );
      rutaRepository.guardar(new RutaDeReparto(UUID.fromString("f5d29506-f335-4c33-bc40-19315c2fe173"), LocalDate.now().minusDays(1), true, c3, f3, paradasRuta3));

      List<Parada> paradasRuta4 = List.of(
          new Parada(1, new Direccion("Av. del Libertador", "1000", "Vicente López"), new Coordenada(-34.5298, -58.4732),
              List.of(new Entrega(UUID.fromString("139e0f9f-9431-4302-9f06-cad97978f7be"), EstadoEntrega.EN_TRASLADO, 50.0, 0.4, null)))
      );
      rutaRepository.guardar(new RutaDeReparto(UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890"), LocalDate.now(), false, c5, f5, paradasRuta4));

      log.info(">>> [MongoDataInitializer] 4 Rutas de Reparto precargadas exitosamente.");
    } else {
      log.info(">>> [MongoDataInitializer] La base de datos ya contiene registros. Omitiendo precarga.");
    }
  }
}
