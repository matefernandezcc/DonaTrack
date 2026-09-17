package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("Recurrente")
@Getter
@Setter
public class NecesidadRecurrenteEntity extends NecesidadEntity {
}
