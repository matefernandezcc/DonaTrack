package com.donatrack.incentivos.domain.service;

import com.donatrack.incentivos.domain.model.InsigniaObtenidaEvent;
import com.donatrack.incentivos.domain.model.PerfilDonante;
import java.util.List;

public interface DifusionAdapter {
    void difundirInsignia(InsigniaObtenidaEvent evento);
}
