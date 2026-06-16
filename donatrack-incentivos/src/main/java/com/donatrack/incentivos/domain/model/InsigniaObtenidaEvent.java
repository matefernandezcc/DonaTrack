package com.donatrack.incentivos.domain.model;

import java.util.UUID;

public class InsigniaObtenidaEvent {
    private final UUID donanteId;
    private final Insignia insignia;

    public InsigniaObtenidaEvent(UUID donanteId, Insignia insignia) {
        this.donanteId = donanteId;
        this.insignia = insignia;
    }

    public UUID getDonanteId() {
        return donanteId;
    }

    public Insignia getInsignia() {
        return insignia;
    }
}
