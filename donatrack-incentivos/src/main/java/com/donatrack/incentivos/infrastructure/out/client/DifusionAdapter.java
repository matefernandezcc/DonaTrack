package com.donatrack.incentivos.infrastructure.out.client;

import com.donatrack.incentivos.domain.entities.InsigniaObtenidaEvent;
public interface DifusionAdapter {
    void difundirInsignia(InsigniaObtenidaEvent evento);
}
