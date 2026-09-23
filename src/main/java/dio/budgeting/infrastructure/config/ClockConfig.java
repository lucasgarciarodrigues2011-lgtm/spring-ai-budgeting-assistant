package dio.budgeting.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * "Hoje" depende do fuso horário. Um Clock injetável deixa isso explícito e permite
 * fixar a data nos testes.
 */
@Configuration
public class ClockConfig {

    @Bean
    Clock clock(@Value("${budgeting.zone:America/Sao_Paulo}") String zone) {
        return Clock.system(ZoneId.of(zone));
    }
}
