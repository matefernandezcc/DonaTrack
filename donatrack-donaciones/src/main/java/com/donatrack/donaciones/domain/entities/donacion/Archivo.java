package com.donatrack.donaciones.domain.entities.donacion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Archivo {
    private String nombre;
    private byte[] contenido;
}
