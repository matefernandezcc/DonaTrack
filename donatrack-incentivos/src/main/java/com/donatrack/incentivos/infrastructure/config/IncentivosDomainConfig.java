package com.donatrack.incentivos.infrastructure.config;

import com.donatrack.incentivos.domain.model.ranking.RankingMisionesStrategy;
import com.donatrack.incentivos.domain.repository.PerfilDonanteRepository;
import com.donatrack.incentivos.domain.service.RankingMensualService;
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
}
