package com.donatrack.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionInicioRutaEvent {
    private UUID rutaId;
    private String patenteCamion;
    private String nombreChofer;
    private List<ContactoInfo> contactosDonantes;
    private List<ContactoInfo> contactosEntidades;
    private String linkSeguimiento;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactoInfo {
        private String destinatario; // Email o número
        private String medio; // "EMAIL", "SMS", "WHATSAPP"
        private String rol; // "DONANTE", "ENTIDAD_BENEFICIARIA"
    }
}
