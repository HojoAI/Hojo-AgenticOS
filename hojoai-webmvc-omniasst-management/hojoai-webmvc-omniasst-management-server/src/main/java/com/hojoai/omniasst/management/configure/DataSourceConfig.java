/*
 * Copyright 2026 HojoAI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hojoai.omniasst.management.configure;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * 多数据源：MySQL（主，MyBatis-Plus）与可选 PostgreSQL（JdbcTemplate，声纹等）。
 */
@Configuration
public class DataSourceConfig {

    // MySQL DataSource (Primary for MyBatis-Plus)
    @Value("${spring.datasource.url}")
    private String mysqlUrl;
    @Value("${spring.datasource.username}")
    private String mysqlUsername;
    @Value("${spring.datasource.password}")
    private String mysqlPassword;
    @Value("${spring.datasource.driver-class-name}")
    private String mysqlDriverClassName;

    // PostgreSQL DataSource (Secondary for JdbcTemplate)
    @Value("${spring.datasource.postgresql.url:}")
    private String postgresqlUrl;
    @Value("${spring.datasource.postgresql.username:}")
    private String postgresqlUsername;
    @Value("${spring.datasource.postgresql.password:}")
    private String postgresqlPassword;

    @Primary
    @Bean(name = "dataSource")
    public DataSource mysqlDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(mysqlDriverClassName);
        dataSource.setUrl(mysqlUrl);
        dataSource.setUsername(mysqlUsername);
        dataSource.setPassword(mysqlPassword);
        return dataSource;
    }

    @Bean(name = "postgresqlDataSource")
    @ConditionalOnProperty(name = "spring.datasource.postgresql.url")
    public DataSource postgresqlDataSource() {
        if (postgresqlUrl == null || postgresqlUrl.isEmpty()) {
            return null;
        }
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(postgresqlUrl);
        dataSource.setUsername(postgresqlUsername);
        dataSource.setPassword(postgresqlPassword);
        return dataSource;
    }

    @Bean(name = "postgresqlJdbcTemplate")
    public JdbcTemplate postgresqlJdbcTemplate(@Qualifier("postgresqlDataSource") DataSource postgresqlDataSource) {
        if (postgresqlDataSource == null) {
            return null;
        }
        return new JdbcTemplate(postgresqlDataSource);
    }
}
