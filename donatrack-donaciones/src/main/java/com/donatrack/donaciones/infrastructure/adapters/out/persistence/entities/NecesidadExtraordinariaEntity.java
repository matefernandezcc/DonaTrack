package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("Extraordinaria")
@Getter
@Setter
public class NecesidadExtraordinariaEntity extends NecesidadEntity {}
