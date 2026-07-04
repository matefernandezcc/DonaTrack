package com.donatrack.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionEntregaFallidaEvent {
    private UUID idDonacion;
    private String motivo;
    private boolean puedeReplanificarse;
    private List<NotificacionInicioRutaEvent.ContactoInfo> contactosDonantes;
    private List<NotificacionInicioRutaEvent.ContactoInfo> contactosEntidades;
    private List<NotificacionInicioRutaEvent.ContactoInfo> contactosAdministradores;
}
