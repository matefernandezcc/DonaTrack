package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "personas", schema = "donaciones")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class PersonaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "persona_id")
  private UUID id;

  @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "direccion_id")
  private DireccionEntity direccion;

  @Column(name = "email")
  private String email;

  @Column(name = "doc_tipo")
  private String docTipo;

  @Column(name = "doc_numero")
  private String docNumero;

  @Column(name = "contacto_correo")
  private String contactoCorreo;

  @Column(name = "contacto_telefono")
  private String contactoTelefono;

  @Column(name = "contacto_whatsapp")
  private String contactoWhatsapp;

  @Column(name = "contacto_medio_predeterminado")
  private String contactoMedioPredeterminado;

  @Column(name = "nacionalidad")
  private String nacionalidad;

  @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RolEntity> roles = new ArrayList<>();
}
