package com.donatrack.auth.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "usuarios", schema = "auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "usuario_id")
  private UUID id;

  @Column(nullable = false, unique = true, length = 100)
  private String username;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String nombre;

  @Column(nullable = false, unique = true)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private Rol rol;

  @Column(name = "persona_id")
  private UUID personaId;

  @Column(name = "proveedor_oauth", length = 50)
  @Builder.Default
  private String proveedorOAuth = "LOCAL";

  @Column(name = "fecha_creacion")
  @Builder.Default
  private LocalDateTime fechaCreacion = LocalDateTime.now();
}

