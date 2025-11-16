package ru.sushi.delivery.kds.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class MultiCityDataSourceConfig {

    // Парнас DataSource
    @Bean
    @Qualifier("parnasDataSource")
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
        return new HikariDataSource(config);
    }

    // Ухта DataSource
    @Bean
    @Qualifier("ukhtaDataSource")
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
        return new HikariDataSource(config);
    }

    @Bean
    @Qualifier("parnasJdbcTemplate")
    public JdbcTemplate parnasJdbcTemplate(@Qualifier("parnasDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @Qualifier("ukhtaJdbcTemplate")
    public JdbcTemplate ukhtaJdbcTemplate(@Qualifier("ukhtaDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}

