package com.orbitet.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    /** Single-broker dev topology — replication factor 1 is the ceiling until there's more than one broker. */
    @Bean
    public NewTopic userCreatedTopic() {
        return new NewTopic("user_created_topic", 3, (short) 1);
    }
}
