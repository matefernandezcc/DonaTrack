package com.donatrack.incentivos.infrastructure.config;

import com.donatrack.incentivos.domain.entities.ranking.RankingMisionesStrategy;
import com.donatrack.incentivos.application.ports.out.PerfilDonanteRepository;
import com.donatrack.incentivos.domain.services.RankingMensualService;
import com.donatrack.incentivos.application.usecases.RegistrarActividadDonacionUseCase;
import com.donatrack.incentivos.application.ports.out.IncentivosNotificacionPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IncentivosDomainConfig {

    @Bean
    public RankingMisionesStrategy RankingMisionesStrategy() {
        return new RankingMisionesStrategy();
    }

    @Bean
    public RankingMensualService rankingMensualService(
            PerfilDonanteRepository repository,
            RankingMisionesStrategy strategy) {
        return new RankingMensualService(repository, strategy);
    }

    @Bean
    public RegistrarActividadDonacionUseCase registrarActividadDonacionUseCase(
            IncentivosNotificacionPort notificacionPort,
            ApplicationEventPublisher eventPublisher,
            PerfilDonanteRepository perfilDonanteRepository) {
        return new RegistrarActividadDonacionUseCase(notificacionPort, eventPublisher, perfilDonanteRepository);
    }
}
