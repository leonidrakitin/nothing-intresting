package ru.sushi.delivery.kds.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Конфигурация для дополнительных DataSource (Парнас и Ухта).
 * Эти DataSource используются только для чтения данных через JDBC,
 * не конфликтуют с основным DataSource из application.yaml,
 * который используется для JPA и EntityManagerFactory.
 */
@Configuration
public class MultiCityDataSourceConfig {

    // Парнас DataSource - создается только для мульти-города, не конфликтует с основным
    // Используем явное имя бина, чтобы не конфликтовать с основным DataSource
    @Bean(name = "parnasDataSource")
    @Qualifier("parnasDataSource")
    @ConfigurationProperties(prefix = "parnas.datasource")
    public DataSource parnasDataSource(Environment env) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(env.getProperty("parnas.datasource.url", 
            "jdbc:postgresql://217.18.61.192:5432/kds-spb"));
        config.setUsername(env.getProperty("parnas.datasource.username", "adm"));
        config.setPassword(env.getProperty("parnas.datasource.password", "12341234s"));
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setPoolName("ParnasPool");
        // Важно: не используем этот DataSource для JPA
        return new HikariDataSource(config);
    }

    // Ухта DataSource - создается только для мульти-города, не конфликтует с основным
    @Bean(name = "ukhtaDataSource")
    @Qualifier("ukhtaDataSource")
    @ConfigurationProperties(prefix = "ukhta.datasource")
    public DataSource ukhtaDataSource(Environment env) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(env.getProperty("ukhta.datasource.url", 
            "jdbc:postgresql://217.18.61.192:5432/kds-ukhta"));
        config.setUsername(env.getProperty("ukhta.datasource.username", "adm"));
        config.setPassword(env.getProperty("ukhta.datasource.password", "12341234s"));
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setPoolName("UkhtaPool");
        // Важно: не используем этот DataSource для JPA
        return new HikariDataSource(config);
    }

    @Bean(name = "parnasJdbcTemplate")
    @Qualifier("parnasJdbcTemplate")
    public JdbcTemplate parnasJdbcTemplate(@Qualifier("parnasDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "ukhtaJdbcTemplate")
    @Qualifier("ukhtaJdbcTemplate")
    public JdbcTemplate ukhtaJdbcTemplate(@Qualifier("ukhtaDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}

