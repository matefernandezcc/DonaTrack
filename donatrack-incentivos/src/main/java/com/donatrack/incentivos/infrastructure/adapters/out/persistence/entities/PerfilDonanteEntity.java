package com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "perfiles_donante", schema = "incentivos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerfilDonanteEntity {

    @Id
    @Column(name = "donante_id")
    private UUID donanteId;

    @Column(name = "categoria", nullable = false)
    private String categoria;

    @Column(name = "fecha_corte_racha")
    private LocalDate fechaCorteRacha;

    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InsigniaEntity> insigniasObtenidas = new ArrayList<>();

    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RegistroDonacionEntity> registrosDonacion = new ArrayList<>();

    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MisionCompletadaEntity> misionesCompletadas = new ArrayList<>();
}
