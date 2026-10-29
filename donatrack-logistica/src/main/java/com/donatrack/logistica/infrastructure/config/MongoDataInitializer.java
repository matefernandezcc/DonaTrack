package com.donatrack.logistica.infrastructure.config;

import com.donatrack.logistica.application.ports.out.CamionRepositoryPort;
import com.donatrack.logistica.application.ports.out.ChoferRepositoryPort;
import com.donatrack.logistica.application.ports.out.ItemPlanificacionRepositoryPort;
import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepositoryPort;
import com.donatrack.logistica.domain.entities.entregas.ComprobanteRecepcion;
import com.donatrack.logistica.domain.entities.entregas.Entrega;
import com.donatrack.logistica.domain.entities.entregas.EstadoEntrega;
import com.donatrack.logistica.domain.entities.planificacion.ItemPlanificacion;
import com.donatrack.logistica.domain.entities.reparto.Camion;
import com.donatrack.logistica.domain.entities.reparto.Chofer;
import com.donatrack.logistica.domain.entities.reparto.Coordenada;
import com.donatrack.logistica.domain.entities.reparto.Direccion;
import com.donatrack.logistica.domain.entities.reparto.Parada;
import com.donatrack.logistica.domain.entities.reparto.RutaDeReparto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Precarga de datos enriquecidos en MongoDB (para entorno local y nube cuando la base esté vacía).
 * Asegura la disponibilidad inmediata de entidades de prueba para verificar flujos y colecciones de Bruno.
 */
@Component
@Profile("!test")
public class MongoDataInitializer implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(MongoDataInitializer.class);

  private final CamionRepositoryPort camionRepository;
  private final ChoferRepositoryPort choferRepository;
  private final ItemPlanificacionRepositoryPort itemPlanificacionRepository;
  private final RutaDeRepartoRepositoryPort rutaRepository;

  public MongoDataInitializer(
      CamionRepositoryPort camionRepository,
      ChoferRepositoryPort choferRepository,
      ItemPlanificacionRepositoryPort itemPlanificacionRepository,
      RutaDeRepartoRepositoryPort rutaRepository) {
    this.camionRepository = camionRepository;
    this.choferRepository = choferRepository;
    this.itemPlanificacionRepository = itemPlanificacionRepository;
    this.rutaRepository = rutaRepository;
  }

  @Override
  public void run(String... args) {
    if (camionRepository.obtenerTodos().isEmpty()) {
      log.info(">>> [MongoDataInitializer] Base de datos vacía detectada. Iniciando precarga de datos enriquecidos...");

      // 1. Precarga de 6 Camiones
      Camion camion1 = new Camion(
          UUID.fromString("c1111111-1111-4111-8111-111111111111"), "AA123BB", 20.0, 3.2, 4000.0);
      Camion camion2 = new Camion(
          UUID.fromString("c2222222-2222-4222-8222-222222222222"), "AF456CD", 12.5, 2.8, 2500.0);
      Camion camion3 = new Camion(
          UUID.fromString("c3333333-3333-4333-8333-333333333333"), "AC789EE", 30.0, 3.8, 8000.0);
      Camion camion4 = new Camion(
          UUID.fromString("c4444444-4444-4444-8444-444444444444"), "AD102ZX", 15.0, 3.0, 3200.0);
      Camion camion5 = new Camion(
          UUID.fromString("c5555555-5555-4555-8555-555555555555"), "AE304KL", 25.0, 3.5, 5500.0);
      Camion camion6 = new Camion(
          UUID.fromString("c6666666-6666-4666-8666-666666666666"), "AG555OP", 10.0, 2.5, 1800.0);

      camionRepository.guardar(camion1);
      camionRepository.guardar(camion2);
      camionRepository.guardar(camion3);
      camionRepository.guardar(camion4);
      camionRepository.guardar(camion5);
      camionRepository.guardar(camion6);
      log.info(">>> [MongoDataInitializer] 6 Camiones precargados exitosamente.");

      // 2. Precarga de 6 Choferes
      Chofer chofer1 = new Chofer(
          UUID.fromString("f1111111-1111-4111-8111-111111111111"), "CH-101", "Roberto Gómez");
      Chofer chofer2 = new Chofer(
          UUID.fromString("f2222222-2222-4222-8222-222222222222"), "CH-102", "Laura Fernández");
      Chofer chofer3 = new Chofer(
          UUID.fromString("f3333333-3333-4333-8333-333333333333"), "CH-103", "Carlos Atlas");
      Chofer chofer4 = new Chofer(
          UUID.fromString("f4444444-4444-4444-8444-444444444444"), "CH-104", "Martín Benítez");
      Chofer chofer5 = new Chofer(
          UUID.fromString("f5555555-5555-4555-8555-555555555555"), "CH-105", "Sofía Morales");
      Chofer chofer6 = new Chofer(
          UUID.fromString("f6666666-6666-4666-8666-666666666666"), "CH-106", "Esteban Rossi");

      choferRepository.guardar(chofer1);
      choferRepository.guardar(chofer2);
      choferRepository.guardar(chofer3);
      choferRepository.guardar(chofer4);
      choferRepository.guardar(chofer5);
      choferRepository.guardar(chofer6);
      log.info(">>> [MongoDataInitializer] 6 Choferes precargados exitosamente.");

      // 3. Precarga de 6 Items de Planificación en distintas zonas
      itemPlanificacionRepository.guardar(new ItemPlanificacion(
          UUID.fromString("31008064-071a-4d7a-ac37-33318f7d9841"), 120.0, 0.8,
          new Direccion("Av. Corrientes", "1234", "CABA")));
      itemPlanificacionRepository.guardar(new ItemPlanificacion(
          UUID.fromString("32008064-071a-4d7a-ac37-33318f7d9842"), 250.0, 1.5,
          new Direccion("Calle 50", "742", "La Plata")));
      itemPlanificacionRepository.guardar(new ItemPlanificacion(
          UUID.fromString("33008064-071a-4d7a-ac37-33318f7d9843"), 85.0, 0.5,
          new Direccion("Av. Santa Fe", "3200", "Palermo, CABA")));
      itemPlanificacionRepository.guardar(new ItemPlanificacion(
          UUID.fromString("34008064-071a-4d7a-ac37-33318f7d9844"), 310.0, 2.1,
          new Direccion("Belgrano", "450", "Quilmes")));
      itemPlanificacionRepository.guardar(new ItemPlanificacion(
          UUID.fromString("35008064-071a-4d7a-ac37-33318f7d9845"), 175.0, 1.1,
          new Direccion("Av. Centenario", "1820", "San Isidro")));
      itemPlanificacionRepository.guardar(new ItemPlanificacion(
          UUID.fromString("36008064-071a-4d7a-ac37-33318f7d9846"), 95.0, 0.6,
          new Direccion("Defensa", "800", "San Telmo, CABA")));
      log.info(">>> [MongoDataInitializer] 6 Items de Planificación precargados exitosamente.");

      // 4. Precarga de 3 Rutas de Reparto (Pendiente, En Curso y Finalizada)
      // RUTA 1: Pendiente (2 paradas)
      List<Parada> paradasRuta1 = new ArrayList<>();
      paradasRuta1.add(new Parada(1, new Direccion("Av. Corrientes", "1234", "CABA"),
          new Coordenada(-34.6037, -58.3816),
          List.of(new Entrega(UUID.fromString("31008064-071a-4d7a-ac37-33318f7d9841"), EstadoEntrega.PENDIENTE, 120.0, 0.8, null))));
      paradasRuta1.add(new Parada(2, new Direccion("Defensa", "800", "San Telmo, CABA"),
          new Coordenada(-34.6186, -58.3712),
          List.of(new Entrega(UUID.fromString("36008064-071a-4d7a-ac37-33318f7d9846"), EstadoEntrega.PENDIENTE, 95.0, 0.6, null))));

      RutaDeReparto ruta1 = new RutaDeReparto(
          UUID.fromString("d3b07384-d113-4a11-9a28-97193a0dc951"), LocalDate.now(), false, camion1, chofer1, paradasRuta1);
      rutaRepository.guardar(ruta1);

      // RUTA 2: En curso (1 entregada con fotos, 1 en traslado)
      List<Parada> paradasRuta2 = new ArrayList<>();
      Entrega entregaEntregada = new Entrega(
          UUID.fromString("33008064-071a-4d7a-ac37-33318f7d9843"), EstadoEntrega.ENTREGADA, 85.0, 0.5,
          new ComprobanteRecepcion(LocalDateTime.now().minusHours(1),
              Arrays.asList("https://storage.donatrack.org/remitos/remito-3300.jpg", "https://storage.donatrack.org/fotos/caja-alimentos-1.jpg"),
              "AF456CD"));
      paradasRuta2.add(new Parada(1, new Direccion("Av. Santa Fe", "3200", "Palermo, CABA"),
          new Coordenada(-34.5863, -58.4103), List.of(entregaEntregada)));

      Entrega entregaEnTraslado = new Entrega(
          UUID.fromString("35008064-071a-4d7a-ac37-33318f7d9845"), EstadoEntrega.EN_TRASLADO, 175.0, 1.1, null);
      paradasRuta2.add(new Parada(2, new Direccion("Av. Centenario", "1820", "San Isidro"),
          new Coordenada(-34.4716, -58.5284), List.of(entregaEnTraslado)));

      RutaDeReparto ruta2 = new RutaDeReparto(
          UUID.fromString("e4c18495-e224-4b22-ab39-08204b1ed062"), LocalDate.now(), true, camion2, chofer2, paradasRuta2);
      rutaRepository.guardar(ruta2);

      // RUTA 3: Finalizada histórica
      Entrega entregaHistorica = new Entrega(
          UUID.fromString("32008064-071a-4d7a-ac37-33318f7d9842"), EstadoEntrega.ENTREGADA, 250.0, 1.5,
          new ComprobanteRecepcion(LocalDateTime.now().minusDays(1),
              Arrays.asList("https://storage.donatrack.org/remitos/remito-lp-742.pdf", "https://storage.donatrack.org/fotos/descarga-fundacion.jpg"),
              "AC789EE"));
      Parada paradaHistorica = new Parada(1, new Direccion("Calle 50", "742", "La Plata"),
          new Coordenada(-34.9214, -57.9545), List.of(entregaHistorica));

      RutaDeReparto ruta3 = new RutaDeReparto(
          UUID.fromString("f5d29506-f335-4c33-bc40-19315c2fe173"), LocalDate.now().minusDays(1), true, camion3, chofer3, List.of(paradaHistorica));
      rutaRepository.guardar(ruta3);

      log.info(">>> [MongoDataInitializer] 3 Rutas de Reparto de prueba precargadas exitosamente.");
    } else {
      log.info(">>> [MongoDataInitializer] La base de datos ya contiene registros. Omitiendo precarga.");
    }
  }
}
