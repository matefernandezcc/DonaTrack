package com.donatrack.donaciones.infrastructure.adapters.in.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.BienEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.CategoriaEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonacionEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonacionOriginalEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.SubcategoriaEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.DonacionJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.test.tester.GraphQlTester;

@GraphQlTest(DonacionGraphQLController.class)
class DonacionGraphQLControllerTest {

  @Autowired private GraphQlTester graphQlTester;

  @MockBean private DonacionJpaRepository donacionJpaRepository;

  private DonacionEntity crearDonacionMock(UUID id, String estado, String desc, String bienDesc) {
    DonacionEntity entity = new DonacionEntity();
    entity.setId(id);
    entity.setEstado(estado);
    entity.setFechaCreacion(LocalDateTime.of(2026, 11, 14, 10, 0));

    DonacionOriginalEntity orig = new DonacionOriginalEntity();
    orig.setDescripcionGeneral(desc);
    entity.setDonacionOriginal(orig);

    CategoriaEntity cat = new CategoriaEntity();
    cat.setNombre("Alimentos");

    SubcategoriaEntity sub = new SubcategoriaEntity();
    sub.setNombre("No Perecederos");
    sub.setCategoria(cat);
    entity.setSubcategoriaAsignada(sub);

    BienEntity bien = new BienEntity();
    bien.setId(UUID.randomUUID());
    bien.setDescripcion(bienDesc);
    bien.setCantidad(5.0);
    entity.setBienes(List.of(bien));

    return entity;
  }

  @Test
  @DisplayName("GraphQL: Consulta donaciones paginada con filtros y proyección de items")
  void testConsultaDonacionesPaginada() {
    UUID donacionId = UUID.randomUUID();
    DonacionEntity donacion =
        crearDonacionMock(donacionId, "EN_DEPOSITO", "Caja de alimentos secos", "Leche en polvo");

    when(donacionJpaRepository.buscarConFiltrosPaginados(
            eq("EN_DEPOSITO"), any(), any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(donacion)));

    String document =
        """
        query {
          donaciones(estado: EN_DEPOSITO, pagina: 0, tamanio: 10) {
            totalElementos
            totalPaginas
            paginaActual
            contenido {
              id
              descripcion
              estado
              items {
                nombre
                cantidad
                categoria
              }
            }
          }
        }
        """;

    graphQlTester
        .document(document)
        .execute()
        .path("donaciones.totalElementos")
        .entity(Integer.class)
        .isEqualTo(1)
        .path("donaciones.contenido[0].id")
        .entity(String.class)
        .isEqualTo(donacionId.toString())
        .path("donaciones.contenido[0].estado")
        .entity(String.class)
        .isEqualTo("EN_DEPOSITO")
        .path("donaciones.contenido[0].items[0].nombre")
        .entity(String.class)
        .isEqualTo("Leche en polvo")
        .path("donaciones.contenido[0].items[0].categoria")
        .entity(String.class)
        .isEqualTo("Alimentos");
  }

  @Test
  @DisplayName("GraphQL: Consulta donacion por ID específico")
  void testConsultaDonacionPorId() {
    UUID donacionId = UUID.randomUUID();
    DonacionEntity donacion =
        crearDonacionMock(donacionId, "ASIGNADA", "Donación asignada a comedor", "Arroz 1kg");

    when(donacionJpaRepository.buscarPorIdConBienes(donacionId))
        .thenReturn(Optional.of(donacion));

    String document =
        """
        query ObtenerDonacion($id: ID!) {
          donacion(id: $id) {
            id
            descripcion
            estado
            items {
              nombre
              cantidad
            }
          }
        }
        """;

    graphQlTester
        .document(document)
        .variable("id", donacionId.toString())
        .execute()
        .path("donacion.id")
        .entity(String.class)
        .isEqualTo(donacionId.toString())
        .path("donacion.estado")
        .entity(String.class)
        .isEqualTo("ASIGNADA")
        .path("donacion.items[0].nombre")
        .entity(String.class)
        .isEqualTo("Arroz 1kg");
  }

  @Test
  @DisplayName("GraphQL: Consulta estadísticas de donante histórico")
  void testConsultaEstadisticasDonante() {
    UUID donanteId = UUID.randomUUID();

    when(donacionJpaRepository.contarPorDonante(donanteId)).thenReturn(12L);
    when(donacionJpaRepository.contarEntregadasPorDonante(donanteId)).thenReturn(8L);

    String document =
        """
        query Stats($donanteId: ID!) {
          estadisticasDonante(idDonante: $donanteId) {
            idDonante
            totalDonaciones
            donacionesEntregadas
          }
        }
        """;

    graphQlTester
        .document(document)
        .variable("donanteId", donanteId.toString())
        .execute()
        .path("estadisticasDonante.totalDonaciones")
        .entity(Integer.class)
        .isEqualTo(12)
        .path("estadisticasDonante.donacionesEntregadas")
        .entity(Integer.class)
        .isEqualTo(8);
  }

  @Test
  @DisplayName("GraphQL: Consulta donacion inexistente retorna null defensivamente")
  void testConsultaDonacionInexistenteRetornaNull() {
    UUID donacionId = UUID.randomUUID();
    when(donacionJpaRepository.buscarPorIdConBienes(donacionId)).thenReturn(Optional.empty());

    String document =
        """
        query ObtenerDonacion($id: ID!) {
          donacion(id: $id) {
            id
            descripcion
          }
        }
        """;

    graphQlTester
        .document(document)
        .variable("id", donacionId.toString())
        .execute()
        .path("donacion")
        .valueIsNull();
  }

  @Test
  @DisplayName("GraphQL: Consulta donacion con UUID malformado retorna null sin lanzar 500")
  void testConsultaDonacionConIdInvalidoRetornaNull() {
    String document =
        """
        query {
          donacion(id: "formato-no-uuid-invalido") {
            id
            descripcion
          }
        }
        """;

    graphQlTester.document(document).execute().path("donacion").valueIsNull();
  }

  @Test
  @DisplayName("GraphQL: Consulta estadisticas con UUID donante invalido retorna ceros")
  void testConsultaEstadisticasConDonanteInvalidoRetornaCeros() {
    String document =
        """
        query {
          estadisticasDonante(idDonante: "donante-invalido") {
            idDonante
            totalDonaciones
            donacionesEntregadas
          }
        }
        """;

    graphQlTester
        .document(document)
        .execute()
        .path("estadisticasDonante.totalDonaciones")
        .entity(Integer.class)
        .isEqualTo(0)
        .path("estadisticasDonante.donacionesEntregadas")
        .entity(Integer.class)
        .isEqualTo(0);
  }

  @Test
  @DisplayName("GraphQL: Consulta donaciones acota tamanio excesivo a 50 y pagina negativa a 0")
  void testConsultaDonacionesAcotaPaginacionDefensiva() {
    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    when(donacionJpaRepository.buscarConFiltrosPaginados(any(), any(), any(), pageableCaptor.capture()))
        .thenReturn(new PageImpl<>(List.of()));

    String document =
        """
        query {
          donaciones(pagina: -5, tamanio: 150) {
            totalElementos
            contenido {
              id
            }
          }
        }
        """;

    graphQlTester.document(document).execute().path("donaciones.totalElementos").entity(Integer.class).isEqualTo(0);

    Pageable captured = pageableCaptor.getValue();
    assertEquals(0, captured.getPageNumber());
    assertEquals(50, captured.getPageSize());
  }
}
