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
package com.hojoai.gateway.omniasst.voice.configure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * 可选的 PostgreSQL 数据源与 {@link JdbcTemplate}。
 * 当 {@code spring.datasource.postgresql.url} 未配置时返回 {@code null} Bean（需调用方判空）。
 */
@Configuration
public class PostgreSQLConfig {

    @Value("${spring.datasource.postgresql.url:}")
    private String url;

    @Value("${spring.datasource.postgresql.username:}")
    private String username;

    @Value("${spring.datasource.postgresql.password:}")
    private String password;

    /**
     * 从配置构建 PostgreSQL {@link DataSource}；url 为空时不创建。
     *
     * @return 数据源，可能为 {@code null}
     */
    @Bean
    public DataSource postgresqlDataSource() {
        if (url == null || url.isEmpty()) {
            return null;
        }
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    /**
     * 基于 PostgreSQL 数据源的 {@link JdbcTemplate}；数据源为空时返回 {@code null}。
     *
     * @param postgresqlDataSource {@link #postgresqlDataSource()} 产物
     * @return JdbcTemplate，可能为 {@code null}
     */
    @Bean
    public JdbcTemplate postgresqlJdbcTemplate(DataSource postgresqlDataSource) {
        if (postgresqlDataSource == null) {
            return null;
        }
        return new JdbcTemplate(postgresqlDataSource);
    }
}

