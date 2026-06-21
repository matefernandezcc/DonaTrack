package com.donatrack.common.events;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutaIniciadaEvent {
    private UUID rutaId;
    private String patenteCamion;
    private String nombreChofer;
    private List<UUID> idsDonaciones;
}
