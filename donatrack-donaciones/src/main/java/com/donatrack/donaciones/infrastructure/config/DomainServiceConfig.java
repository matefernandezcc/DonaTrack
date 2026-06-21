package com.donatrack.donaciones.infrastructure.config;

import com.donatrack.donaciones.domain.entities.donacion.DonacionFactory;
import com.donatrack.donaciones.domain.entities.donacion.segmentador.EstrategiaSegmentacion;
import com.donatrack.donaciones.domain.entities.donacion.segmentador.SegmentarPorEstado;
import com.donatrack.donaciones.domain.entities.donacion.segmentador.SegmentarPorSubcategoria;
import com.donatrack.donaciones.domain.entities.donacion.segmentador.SegmentarPorVencimiento;
import com.donatrack.donaciones.domain.service.MatchmakerService;
import com.donatrack.donaciones.domain.service.ProcesadorCargaInicial;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DomainServiceConfig {

    @Bean
    public DonacionFactory donacionFactory() {
        return new DonacionFactory();
    }

    @Bean
    public ProcesadorCargaInicial procesadorCargaInicial(DonacionFactory donacionFactory) {
        List<EstrategiaSegmentacion> estrategias = List.of(
            new SegmentarPorSubcategoria(),
            new SegmentarPorEstado(),
            new SegmentarPorVencimiento()
        );
        return new ProcesadorCargaInicial(estrategias, donacionFactory);
    }

    @Bean
    public MatchmakerService matchmakerService() {
        return new MatchmakerService();
    }
}
