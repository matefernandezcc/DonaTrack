package com.donatrack.donaciones.infrastructure.adapters.out.persistence;

import com.donatrack.donaciones.application.ports.out.DonacionOriginalRepository;
import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.DonacionOriginal;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.CategoriaEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonacionEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonacionOriginalEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonanteEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.SubcategoriaEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers.DonacionOriginalMapper;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.CategoriaJpaRepository;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.DonacionOriginalJpaRepository;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.SubcategoriaJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class JpaDonacionOriginalRepository implements DonacionOriginalRepository {

  private final DonacionOriginalJpaRepository jpaRepository;
  private final SubcategoriaJpaRepository subcategoriaJpaRepository;
  private final CategoriaJpaRepository categoriaJpaRepository;

  @PersistenceContext private EntityManager entityManager;

  public JpaDonacionOriginalRepository(DonacionOriginalJpaRepository jpaRepository) {
    this(jpaRepository, null, null);
  }

  @Autowired
  public JpaDonacionOriginalRepository(
      DonacionOriginalJpaRepository jpaRepository,
      SubcategoriaJpaRepository subcategoriaJpaRepository,
      CategoriaJpaRepository categoriaJpaRepository) {
    this.jpaRepository = jpaRepository;
    this.subcategoriaJpaRepository = subcategoriaJpaRepository;
    this.categoriaJpaRepository = categoriaJpaRepository;
  }

  @Override
  public Optional<DonacionOriginal> buscarPorId(UUID id) {
    return jpaRepository.findById(id).map(DonacionOriginalMapper::toDomain);
  }

  @Override
  public Optional<DonacionOriginal> buscarPorIdDonacion(UUID idDonacion) {
    return jpaRepository.findAll().stream()
        .filter(d -> d.getDonaciones().stream().anyMatch(seg -> seg.getId().equals(idDonacion)))
        .map(DonacionOriginalMapper::toDomain)
        .findFirst();
  }

  @Override
  @Transactional
  public void guardar(DonacionOriginal donacionOriginal) {
    DonacionOriginalEntity entity = DonacionOriginalMapper.toEntity(donacionOriginal);

    // Asignar donante con referencia manejada por Hibernate (evita entidad detached)
    if (donacionOriginal.getDonante() != null && donacionOriginal.getDonante().getId() != null) {
      DonanteEntity donanteRef =
          entityManager.getReference(DonanteEntity.class, donacionOriginal.getDonante().getId());
      entity.setDonante(donanteRef);
    }

    if (donacionOriginal.getDonacionesSegmentadas() != null && entity.getDonaciones() != null) {
      for (int i = 0;
          i < Math.min(donacionOriginal.getDonacionesSegmentadas().size(), entity.getDonaciones().size());
          i++) {
        Donacion domSeg = donacionOriginal.getDonacionesSegmentadas().get(i);
        DonacionEntity entSeg = entity.getDonaciones().get(i);
        if (domSeg.getSubCategoria() != null && domSeg.getSubCategoria().getNombre() != null) {
          SubcategoriaEntity subEntity =
              resolverSubcategoria(
                  domSeg.getSubCategoria().getNombre(), domSeg.getSubCategoria().getDescripcion());
          entSeg.setSubcategoriaAsignada(subEntity);
        }
      }
    }

    entityManager.persist(entity);
  }

  private SubcategoriaEntity resolverSubcategoria(String nombre, String descripcion) {
    if (subcategoriaJpaRepository == null) {
      return null;
    }
    Optional<SubcategoriaEntity> existente =
        subcategoriaJpaRepository.findFirstByNombreIgnoreCase(nombre.trim());
    if (existente.isPresent()) {
      return existente.get();
    }
    CategoriaEntity cat = null;
    if (categoriaJpaRepository != null) {
      cat =
          categoriaJpaRepository.findAll().stream()
              .findFirst()
              .orElseGet(
                  () -> {
                    CategoriaEntity nuevaCat = new CategoriaEntity();
                    nuevaCat.setNombre("General");
                    nuevaCat.setDescripcion("Categoría general");
                    return categoriaJpaRepository.save(nuevaCat);
                  });
    }
    SubcategoriaEntity nuevaSub = new SubcategoriaEntity();
    nuevaSub.setNombre(nombre.trim());
    nuevaSub.setDescripcion(descripcion != null ? descripcion : "Subcategoría generada");
    nuevaSub.setCategoria(cat);
    return subcategoriaJpaRepository.save(nuevaSub);
  }
}
