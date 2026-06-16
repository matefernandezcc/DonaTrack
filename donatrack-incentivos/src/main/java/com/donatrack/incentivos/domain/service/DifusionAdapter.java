package com.donatrack.incentivos.domain.service;

import com.donatrack.incentivos.domain.model.InsigniaObtenidaEvent;
public interface DifusionAdapter {
    void difundirInsignia(InsigniaObtenidaEvent evento);
}
