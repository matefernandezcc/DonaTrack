package com.donatrack.donaciones.domain.model;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

public abstract class Rol {
    @Getter @Setter private LocalDate fechaAlta;
    

    public Rol() {
        this.fechaAlta = LocalDate.now(); 
    }

    public Rol(LocalDate fechaAlta) {this.fechaAlta = fechaAlta;}

}