package ru.sushi.delivery.kds.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Конфигурация для основного DataSource из application.yaml.
 * Этот DataSource используется для JPA и EntityManagerFactory.
 * Помечен как @Primary, чтобы Spring Boot использовал его для автоконфигурации JPA.
 * 
 * Создается только если Spring Boot еще не создал DataSource автоматически.
 */
@Configuration
public class PrimaryDataSourceConfig {

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "dataSource")
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "dataSource")
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource primaryDataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    /**
     * Основной JdbcTemplate для работы с основной БД.
     * Помечен как @Primary, чтобы использоваться по умолчанию во всех репозиториях,
     * которые не указывают конкретный @Qualifier.
     */
    @Bean
    @Primary
    public JdbcTemplate jdbcTemplate(DataSource primaryDataSource) {
        return new JdbcTemplate(primaryDataSource);
    }
}

