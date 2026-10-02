package com.donatrack.logistica.infrastructure.adapters.out.persistence;

import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepositoryPort;
import com.donatrack.logistica.domain.entities.reparto.RutaDeReparto;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.RutaDeRepartoMapper;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.RutaDeRepartoJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.RutaDeRepartoEntity;

@Repository
@Transactional(readOnly = true)
public class JpaRutaDeRepartoRepository implements RutaDeRepartoRepositoryPort {

  private final RutaDeRepartoJpaRepository jpaRepository;
  private final EntityManager em;

  public JpaRutaDeRepartoRepository(RutaDeRepartoJpaRepository jpaRepository, EntityManager em) {
    this.jpaRepository = jpaRepository;
    this.em = em;
  }

  @Override
  public Optional<RutaDeReparto> buscarPorIdDonacion(UUID idDonacion) {
    return jpaRepository
        .findFirstByEntregaIdDonacion(idDonacion)
        .map(RutaDeRepartoMapper::toDomain);
  }

  @Override
  public Optional<RutaDeReparto> buscarPorId(UUID idRuta) {
    return jpaRepository.findById(idRuta).map(RutaDeRepartoMapper::toDomain);
  }

  @Override
  public List<RutaDeReparto> obtenerTodas() {
    return jpaRepository.findAll().stream().map(RutaDeRepartoMapper::toDomain).toList();
  }

  @Override
  @Transactional
  public void guardar(RutaDeReparto ruta) {
    RutaDeRepartoEntity entity = RutaDeRepartoMapper.toEntity(ruta);
    if (entity.getCamion() != null && entity.getCamion().getId() != null) {
      entity.setCamion(em.getReference(com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.CamionEntity.class, entity.getCamion().getId()));
    }
    if (entity.getChofer() != null && entity.getChofer().getId() != null) {
      entity.setChofer(em.getReference(com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.ChoferEntity.class, entity.getChofer().getId()));
    }
    jpaRepository.save(entity);
    jpaRepository.flush();
    System.out.println("GUARDAR RUTA COMPLETADO Y FLUSHEADO");
  }
}
