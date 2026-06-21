package com.donatrack.incentivos.infrastructure.config;

import com.donatrack.incentivos.domain.entities.ranking.RankingMisionesStrategy;
import com.donatrack.incentivos.domain.repository.PerfilDonanteRepository;
import com.donatrack.incentivos.domain.service.RankingMensualService;
import com.donatrack.incentivos.application.usecase.RegistrarActividadDonacionUseCase;
import com.donatrack.incentivos.infrastructure.out.client.NotificacionClient;
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
            NotificacionClient notificacionClient,
            ApplicationEventPublisher eventPublisher) {
        return new RegistrarActividadDonacionUseCase(notificacionClient, eventPublisher);
    }
}
