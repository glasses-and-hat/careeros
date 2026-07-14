package com.careeros.job.infrastructure.ingestion;

import com.careeros.config.IngestionProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Provides a {@link RestClient.Builder} pre-configured with ingestion HTTP
 * timeouts for each ATS connector to customize with its own base URL.
 *
 * <p>Deliberately prototype-scoped: each connector calls {@code .baseUrl(...)}
 * on its own builder instance in its constructor. A singleton builder shared
 * across all 5 connectors would have its mutable state overwritten as each
 * one customizes it at startup.
 */
@Configuration
class IngestionRestClientConfig {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    RestClient.Builder atsRestClientBuilder(IngestionProperties properties) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(properties.connectTimeout())
                .withReadTimeout(properties.readTimeout());
        ClientHttpRequestFactory factory = ClientHttpRequestFactories.get(settings);
        return RestClient.builder().requestFactory(factory);
    }
}
