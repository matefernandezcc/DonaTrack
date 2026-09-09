package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "categorias", schema = "donaciones")
@Getter
@Setter
public class CategoriaEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "nombre", nullable = false)
  private String nombre;

  @Column(name = "descripcion")
  private String descripcion;

  @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SubcategoriaEntity> subcategorias;
}
