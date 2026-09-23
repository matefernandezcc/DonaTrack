package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "subcategorias", schema = "donaciones")
@Getter
@Setter
public class SubcategoriaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "subcategoria_id")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "categoria_id", nullable = false)
  private CategoriaEntity categoria;

  @Column(name = "nombre", nullable = false)
  private String nombre;

  @Column(name = "descripcion")
  private String descripcion;
}
