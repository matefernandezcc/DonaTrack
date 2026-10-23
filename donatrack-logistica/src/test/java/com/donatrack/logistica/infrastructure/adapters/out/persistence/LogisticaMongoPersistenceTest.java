package com.donatrack.logistica.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.donatrack.logistica.domain.entities.entregas.Entrega;
import com.donatrack.logistica.domain.entities.entregas.EstadoEntrega;
import com.donatrack.logistica.domain.entities.reparto.Camion;
import com.donatrack.logistica.domain.entities.reparto.Chofer;
import com.donatrack.logistica.domain.entities.reparto.Coordenada;
import com.donatrack.logistica.domain.entities.reparto.Direccion;
import com.donatrack.logistica.domain.entities.reparto.Parada;
import com.donatrack.logistica.domain.entities.reparto.RutaDeReparto;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.CamionDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ChoferDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.EntregaDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ParadaDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.RutaDeRepartoDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.mongo.CamionMongoMapper;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.mongo.ChoferMongoMapper;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.mongo.RutaDeRepartoMongoMapper;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.mongo.MongoCamionRepository;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.mongo.MongoChoferRepository;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.mongo.MongoEntregaRepository;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.mongo.MongoRutaDeRepartoRepository;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.mongo.CamionMongoRepository;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.mongo.ChoferMongoRepository;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.mongo.EntregaMongoRepository;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.mongo.RutaDeRepartoMongoRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogisticaMongoPersistenceTest {

  @Mock private CamionMongoRepository camionMongoRepository;
  @Mock private ChoferMongoRepository choferMongoRepository;
  @Mock private RutaDeRepartoMongoRepository rutaMongoRepository;
  @Mock private EntregaMongoRepository entregaMongoRepository;

  private MongoCamionRepository mongoCamionRepository;
  private MongoChoferRepository mongoChoferRepository;
  private MongoRutaDeRepartoRepository mongoRutaRepository;
  private MongoEntregaRepository mongoEntregaRepository;

  @BeforeEach
  void setUp() {
    mongoCamionRepository = new MongoCamionRepository(camionMongoRepository);
    mongoChoferRepository = new MongoChoferRepository(choferMongoRepository);
    mongoRutaRepository = new MongoRutaDeRepartoRepository(rutaMongoRepository);
    mongoEntregaRepository = new MongoEntregaRepository(rutaMongoRepository, entregaMongoRepository);
  }

  @Test
  @DisplayName("Debe persistir y recuperar un Camión en MongoDB a través del adaptador ODM")
  void debePersistirYBuscarCamionMongo() {
    UUID camionId = UUID.randomUUID();
    Camion camion = new Camion(camionId, "AA123BB", 20.0, 3.2, 4000.0);
    CamionDocument doc = CamionMongoMapper.toDocument(camion);

    when(camionMongoRepository.findByPatente("AA123BB")).thenReturn(Optional.of(doc));

    mongoCamionRepository.guardar(camion);
    verify(camionMongoRepository).save(any(CamionDocument.class));

    Optional<Camion> encontrado = mongoCamionRepository.buscarPorPatente("AA123BB");
    assertTrue(encontrado.isPresent());
    assertEquals("AA123BB", encontrado.get().getPatente());
    assertEquals(4000.0, encontrado.get().getCapacidadCarga());
  }

  @Test
  @DisplayName("Debe persistir y recuperar un Chofer en MongoDB a través del adaptador ODM")
  void debePersistirYBuscarChoferMongo() {
    UUID choferId = UUID.randomUUID();
    Chofer chofer = new Chofer(choferId, "CH-101", "Roberto Gómez");
    ChoferDocument doc = ChoferMongoMapper.toDocument(chofer);

    when(choferMongoRepository.findByLegajo("CH-101")).thenReturn(Optional.of(doc));

    mongoChoferRepository.guardar(chofer);
    verify(choferMongoRepository).save(any(ChoferDocument.class));

    Optional<Chofer> encontrado = mongoChoferRepository.buscarPorLegajo("CH-101");
    assertTrue(encontrado.isPresent());
    assertEquals("CH-101", encontrado.get().getLegajo());
    assertEquals("Roberto Gómez", encontrado.get().getNombre());
  }

  @Test
  @DisplayName("Debe desnormalizar Camión y Chofer dentro del documento RutaDeRepartoDocument")
  void debeDesnormalizarCamionYChoferEnRutaDocument() {
    UUID rutaId = UUID.randomUUID();
    UUID camionId = UUID.randomUUID();
    UUID choferId = UUID.randomUUID();

    Camion camion = new Camion(camionId, "AF456CD", 15.0, 2.9, 3000.0);
    Chofer chofer = new Chofer(choferId, "CH-202", "Laura Fernández");

    Entrega entrega = new Entrega();
    entrega.setIdEntrega(UUID.randomUUID());
    entrega.setEstado(EstadoEntrega.PENDIENTE);
    entrega.setPesoEstimado(25.0);

    Parada parada = new Parada(1, new Direccion("Av. Mayo", "100", "CABA"), new Coordenada(-34.6, -58.3), List.of(entrega));
    RutaDeReparto ruta = new RutaDeReparto(rutaId, LocalDate.now(), false, camion, chofer, List.of(parada));

    ArgumentCaptor<RutaDeRepartoDocument> captor = ArgumentCaptor.forClass(RutaDeRepartoDocument.class);

    mongoRutaRepository.guardar(ruta);

    verify(rutaMongoRepository).save(captor.capture());
    RutaDeRepartoDocument guardado = captor.getValue();

    // Verificación de desnormalización de Camión
    assertEquals(camionId, guardado.getCamionId());
    assertEquals("AF456CD", guardado.getCamionPatente());
    assertEquals(3000.0, guardado.getCamionCapacidadCarga());

    // Verificación de desnormalización de Chofer
    assertEquals(choferId, guardado.getChoferId());
    assertEquals("CH-202", guardado.getChoferLegajo());
    assertEquals("Laura Fernández", guardado.getChoferNombre());

    // Verificación de parada y entrega embebidas
    assertEquals(1, guardado.getParadas().size());
    assertEquals(1, guardado.getParadas().get(0).getEntregas().size());
    assertEquals(entrega.getIdEntrega(), guardado.getParadas().get(0).getEntregas().get(0).getIdEntrega());
  }

  @Test
  @DisplayName("Debe buscar una Entrega dentro del Aggregate Root RutaDeReparto y actualizar su estado")
  void debeBuscarYActualizarEntregaEmbebidaEnRuta() {
    UUID rutaId = UUID.randomUUID();
    UUID entregaId = UUID.randomUUID();

    EntregaDocument ed = new EntregaDocument(entregaId, EstadoEntrega.EN_TRASLADO, 10.0, 0.2, null);
    ParadaDocument pd = new ParadaDocument(1, null, null, new ArrayList<>(List.of(ed)));
    RutaDeRepartoDocument rutaDoc = new RutaDeRepartoDocument();
    rutaDoc.setId(rutaId);
    rutaDoc.setParadas(new ArrayList<>(List.of(pd)));

    when(rutaMongoRepository.findByEntregaId(entregaId)).thenReturn(Optional.of(rutaDoc));

    // 1. Buscar entrega por ID
    Optional<Entrega> encontrada = mongoEntregaRepository.buscarPorId(entregaId);
    assertTrue(encontrada.isPresent());
    assertEquals(EstadoEntrega.EN_TRASLADO, encontrada.get().getEstado());

    // 2. Modificar y guardar
    Entrega paraActualizar = encontrada.get();
    paraActualizar.confirmarRecepcion(List.of("https://foto.com/remito.jpg"), "AA123BB");

    mongoEntregaRepository.guardar(paraActualizar);

    // Verificar que se persistió en la ruta actualizada
    verify(rutaMongoRepository).save(rutaDoc);
    assertEquals(EstadoEntrega.ENTREGADA, pd.getEntregas().get(0).getEstado());
    assertNotNull(pd.getEntregas().get(0).getComprobanteRecepcion());
    assertEquals("AA123BB", pd.getEntregas().get(0).getComprobanteRecepcion().getCamionPatente());
  }
}
