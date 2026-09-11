package com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "misiones_completadas", schema = "incentivos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MisionCompletadaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donante_id", nullable = false)
    private PerfilDonanteEntity perfil;

    @Column(name = "nombre_mision", nullable = false)
    private String nombreMision;

    @Column(name = "mes_completado", nullable = false)
    private String mesCompletado; // Stored as YYYY-MM
}
