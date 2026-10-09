package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "roles_donante", schema = "donaciones")
@PrimaryKeyJoinColumn(name = "rol_donante_id", referencedColumnName = "rol_id")
@DiscriminatorValue("Donante")
@Getter
@Setter
public class DonanteEntity extends RolEntity {}
