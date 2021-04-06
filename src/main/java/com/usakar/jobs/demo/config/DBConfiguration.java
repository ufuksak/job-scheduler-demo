package com.usakar.jobs.demo.config;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DBConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties("com.usakar.jobs.demo.app-db")
    public DataSourceProperties appDBProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource appDBDataSource() {
        return appDBProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    @ConfigurationProperties("com.usakar.jobs.demo.scheduler-db")
    public DataSourceProperties schedulerDBProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @QuartzDataSource
    public DataSource schedulerDBDataSource()    {
        return schedulerDBProperties().initializeDataSourceBuilder().build();
    }
}
