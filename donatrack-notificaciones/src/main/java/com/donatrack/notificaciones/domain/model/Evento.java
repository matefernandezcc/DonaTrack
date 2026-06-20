package com.donatrack.notificaciones.domain.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Evento {
    private TipoEvento tipoEvento;
    private String descripcion;
    private List<String> rolesDestinatarios;

    public boolean validoParaNotificar(String rol) {
        return rolesDestinatarios != null && rolesDestinatarios.contains(rol);
    }
}
