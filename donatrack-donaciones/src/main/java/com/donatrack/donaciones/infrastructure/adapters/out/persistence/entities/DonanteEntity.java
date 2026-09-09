package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("DONANTE")
@Getter
@Setter
public class DonanteEntity extends RolEntity {
  // Las donaciones realizadas se mapean desde DonacionOriginalEntity (relación bidireccional si fuera necesario,
  // pero generalmente la relación principal es desde DonacionOriginal -> RolEntity)
}
