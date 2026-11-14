package com.donatrack.donaciones.infrastructure.adapters.in.graphql;

import com.donatrack.donaciones.infrastructure.adapters.in.graphql.dto.DonacionGraphQLDTO;
import com.donatrack.donaciones.infrastructure.adapters.in.graphql.dto.DonacionPageGraphQLDTO;
import com.donatrack.donaciones.infrastructure.adapters.in.graphql.dto.EstadisticasDonanteGraphQLDTO;
import com.donatrack.donaciones.infrastructure.adapters.in.graphql.dto.ItemDonacionGraphQLDTO;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.BienEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonacionEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.DonacionJpaRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class DonacionGraphQLController {

  private final DonacionJpaRepository donacionJpaRepository;

  public DonacionGraphQLController(DonacionJpaRepository donacionJpaRepository) {
    this.donacionJpaRepository = donacionJpaRepository;
  }

  @QueryMapping
  public DonacionPageGraphQLDTO donaciones(
      @Argument String estado,
      @Argument String idDonante,
      @Argument String categoriaId,
      @Argument Integer pagina,
      @Argument Integer tamanio) {

    int tamanioSeguro = Math.min(Math.max(tamanio != null ? tamanio : 10, 1), 50);
    int paginaSegura = Math.max(pagina != null ? pagina : 0, 0);

    UUID donanteUuid = null;
    if (idDonante != null && !idDonante.isBlank()) {
      try {
        donanteUuid = UUID.fromString(idDonante);
      } catch (IllegalArgumentException ignored) {
      }
    }

    UUID catUuid = null;
    if (categoriaId != null && !categoriaId.isBlank()) {
      try {
        catUuid = UUID.fromString(categoriaId);
      } catch (IllegalArgumentException ignored) {
      }
    }

    Pageable pageable =
        PageRequest.of(
            paginaSegura, tamanioSeguro, Sort.by(Sort.Direction.DESC, "fechaCreacion"));
    Page<DonacionEntity> page =
        donacionJpaRepository.buscarConFiltrosPaginados(estado, donanteUuid, catUuid, pageable);

    List<DonacionGraphQLDTO> items =
        page.getContent().stream().map(this::mapToGraphQLDTO).toList();

    return new DonacionPageGraphQLDTO(
        items, (int) page.getTotalElements(), page.getTotalPages(), page.getNumber());
  }

  @QueryMapping
  public DonacionGraphQLDTO donacion(@Argument String id) {
    if (id == null || id.isBlank()) {
      return null;
    }
    try {
      UUID uuid = UUID.fromString(id);
      return donacionJpaRepository
          .buscarPorIdConBienes(uuid)
          .map(this::mapToGraphQLDTO)
          .orElse(null);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  @QueryMapping
  public EstadisticasDonanteGraphQLDTO estadisticasDonante(@Argument String idDonante) {
    if (idDonante == null || idDonante.isBlank()) {
      return new EstadisticasDonanteGraphQLDTO(UUID.fromString("00000000-0000-0000-0000-000000000000"), 0, 0);
    }
    try {
      UUID donanteUuid = UUID.fromString(idDonante);
      long total = donacionJpaRepository.contarPorDonante(donanteUuid);
      long entregadas = donacionJpaRepository.contarEntregadasPorDonante(donanteUuid);
      return new EstadisticasDonanteGraphQLDTO(donanteUuid, (int) total, (int) entregadas);
    } catch (IllegalArgumentException e) {
      return new EstadisticasDonanteGraphQLDTO(
          UUID.nameUUIDFromBytes(idDonante.getBytes(java.nio.charset.StandardCharsets.UTF_8)), 0, 0);
    }
  }

  private DonacionGraphQLDTO mapToGraphQLDTO(DonacionEntity entity) {
    String desc =
        entity.getDonacionOriginal() != null
                && entity.getDonacionOriginal().getDescripcionGeneral() != null
            ? entity.getDonacionOriginal().getDescripcionGeneral()
            : "Donación " + entity.getId();

    String fecha =
        entity.getFechaCreacion() != null
            ? entity.getFechaCreacion().toString()
            : LocalDateTime.now().toString();

    String categoriaNombre = "General";
    if (entity.getSubcategoriaAsignada() != null) {
      if (entity.getSubcategoriaAsignada().getCategoria() != null) {
        categoriaNombre = entity.getSubcategoriaAsignada().getCategoria().getNombre();
      } else if (entity.getSubcategoriaAsignada().getNombre() != null) {
        categoriaNombre = entity.getSubcategoriaAsignada().getNombre();
      }
    }

    List<ItemDonacionGraphQLDTO> items = new ArrayList<>();
    if (entity.getBienes() != null) {
      final String cat = categoriaNombre;
      for (BienEntity b : entity.getBienes()) {
        items.add(
            new ItemDonacionGraphQLDTO(
                b.getId() != null ? b.getId() : UUID.randomUUID(),
                b.getDescripcion() != null ? b.getDescripcion() : "Bien",
                b.getCantidad() != null ? b.getCantidad().intValue() : 1,
                cat));
      }
    }

    return new DonacionGraphQLDTO(
        entity.getId(), desc, entity.getEstado(), fecha, Collections.unmodifiableList(items));
  }
}
