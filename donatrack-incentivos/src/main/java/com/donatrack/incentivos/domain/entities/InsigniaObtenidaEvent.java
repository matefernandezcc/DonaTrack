package com.donatrack.incentivos.domain.entities;

import java.util.UUID;
import lombok.Getter;

@Getter
public class InsigniaObtenidaEvent {
    private final UUID donanteId;
    private final Insignia insignia;

    public InsigniaObtenidaEvent(UUID donanteId, Insignia insignia) {
        this.donanteId = donanteId;
        this.insignia = insignia;
    }
}
