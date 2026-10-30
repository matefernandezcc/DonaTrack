package com.donatrack.donaciones.infrastructure.adapters.out.persistence;

import com.donatrack.donaciones.application.ports.out.BeneficiarioRepository;
import com.donatrack.donaciones.domain.entities.necesidades.Necesidad;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.BeneficiarioEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.CategoriaEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.NecesidadEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.SubcategoriaEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers.RolMapper;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.BeneficiarioJpaRepository;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.CategoriaJpaRepository;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.SubcategoriaJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class JpaBeneficiarioRepository implements BeneficiarioRepository {

  private final BeneficiarioJpaRepository jpaRepository;
  private final SubcategoriaJpaRepository subcategoriaJpaRepository;
  private final CategoriaJpaRepository categoriaJpaRepository;

  @PersistenceContext private EntityManager entityManager;

  public JpaBeneficiarioRepository(BeneficiarioJpaRepository jpaRepository) {
    this(jpaRepository, null, null);
  }

  @Autowired
  public JpaBeneficiarioRepository(
      BeneficiarioJpaRepository jpaRepository,
      SubcategoriaJpaRepository subcategoriaJpaRepository,
      CategoriaJpaRepository categoriaJpaRepository) {
    this.jpaRepository = jpaRepository;
    this.subcategoriaJpaRepository = subcategoriaJpaRepository;
    this.categoriaJpaRepository = categoriaJpaRepository;
  }

  @Override
  public List<Beneficiario> buscarTodos() {
    return jpaRepository.findAll().stream()
        .map(entity -> (Beneficiario) RolMapper.toDomain(entity))
        .toList();
  }

  @Override
  public Optional<Beneficiario> buscarPorId(UUID id) {
    return jpaRepository.findById(id).map(entity -> (Beneficiario) RolMapper.toDomain(entity));
  }

  @Override
  @Transactional
  public Beneficiario guardar(Beneficiario beneficiario) {
    if (beneficiario.getFechaAlta() == null) {
      beneficiario.setFechaAlta(LocalDate.now());
    }
    BeneficiarioEntity entity = (BeneficiarioEntity) RolMapper.toEntity(beneficiario);

    if (beneficiario.getNecesidadesDeclaradas() != null
        && entity.getNecesidadesDeclaradas() != null) {
      for (int i = 0;
          i
              < Math.min(
                  beneficiario.getNecesidadesDeclaradas().size(),
                  entity.getNecesidadesDeclaradas().size());
          i++) {
        Necesidad domNec = beneficiario.getNecesidadesDeclaradas().get(i);
        NecesidadEntity entNec = entity.getNecesidadesDeclaradas().get(i);
        if (domNec.getSubcategoriaRequerida() != null
            && domNec.getSubcategoriaRequerida().getNombre() != null) {
          entNec.setSubcategoriaRequerida(
              resolverSubcategoria(
                  domNec.getSubcategoriaRequerida().getNombre(),
                  domNec.getSubcategoriaRequerida().getDescripcion()));
        }
      }
    }

    if (entity.getId() == null || !jpaRepository.existsById(entity.getId())) {
      if (entity.getId() == null) {
        entity.setId(UUID.randomUUID());
      }
      entityManager.persist(entity);
    } else {
      BeneficiarioEntity existente = jpaRepository.findById(entity.getId()).orElse(null);
      if (existente != null && entity.getPersona() == null) {
        entity.setPersona(existente.getPersona());
      }
      entity = entityManager.merge(entity);
    }
    return (Beneficiario) RolMapper.toDomain(entity);
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

  @Override
  @Transactional
  public void eliminarPorId(UUID id) {
    jpaRepository.deleteById(id);
  }
}
