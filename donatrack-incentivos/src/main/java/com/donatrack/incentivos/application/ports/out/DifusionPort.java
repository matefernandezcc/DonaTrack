package com.donatrack.incentivos.application.ports.out;

import com.donatrack.incentivos.domain.entities.InsigniaObtenidaEvent;
public interface DifusionPort {
    void difundirInsignia(InsigniaObtenidaEvent evento);
}
