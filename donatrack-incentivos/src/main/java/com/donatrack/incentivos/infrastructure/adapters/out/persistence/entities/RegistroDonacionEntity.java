package com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "registros_donacion", schema = "incentivos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroDonacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donante_id", nullable = false)
    private PerfilDonanteEntity perfil;

    @Column(name = "id_donacion", nullable = false)
    private UUID idDonacion;

    @Column(name = "cantidad_bienes")
    private Integer cantidadBienes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "registro_donacion_categorias",
            schema = "incentivos",
            joinColumns = @JoinColumn(name = "registro_id")
    )
    @Column(name = "categoria")
    private Set<String> categorias = new HashSet<>();

    @Column(name = "id_entidad_beneficiaria")
    private UUID idEntidadBeneficiaria;

    @Column(name = "mes_donacion")
    private String mesDonacion; // Stored as YYYY-MM

    @Column(name = "fecha_donacion")
    private LocalDate fechaDonacion;
}
