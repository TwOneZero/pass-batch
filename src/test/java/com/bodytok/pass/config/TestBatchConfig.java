package com.bodytok.pass.config;


import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaAuditing
@EnableAutoConfiguration
@EntityScan("com.bodytok.pass.repository")
@EnableJpaRepositories("com.bodytok.pass.repository")
@EnableTransactionManagement
public class TestBatchConfig {
}
