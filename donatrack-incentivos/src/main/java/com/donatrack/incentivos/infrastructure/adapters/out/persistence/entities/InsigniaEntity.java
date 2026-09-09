package com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "insignias", schema = "incentivos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InsigniaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donante_id", nullable = false)
    private PerfilDonanteEntity perfil;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "url_imagen")
    private String urlImagen;

    @Column(name = "fecha_obtencion")
    private LocalDate fechaObtencion;

    @Column(name = "visible_publicamente")
    private Boolean visiblePublicamente;
}
