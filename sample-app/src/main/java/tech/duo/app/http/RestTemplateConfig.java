package tech.duo.app.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.httpcomponents.hc5.PoolingHttpClientConnectionManagerMetricsBinder;
import io.opentelemetry.api.OpenTelemetry;
//import io.opentelemetry.instrumentation.spring.web.v3_1.SpringWebTelemetry;
//import io.opentelemetry.instrumentation.spring.webmvc.v5_3.SpringWebMvcTelemetry;
import jakarta.servlet.Filter;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
public class RestTemplateConfig {

    @Value("${rest-template.connections.max-total:200}")
    int maxTotal;
    @Value("${rest-template.connections.per-route-max-total:100}")
    int perRouteMaxTotal;

//    @Autowired
//    SpringWebTelemetryBuilder otelBuilder;

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder, MeterRegistry registry, OpenTelemetry openTelemetry) {

        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(perRouteMaxTotal);
        poolingHttpClientConnectionManager.setMaxTotal(maxTotal);
        new PoolingHttpClientConnectionManagerMetricsBinder(poolingHttpClientConnectionManager, "pool").bindTo(registry);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(5000, TimeUnit.MILLISECONDS)
                .setConnectTimeout(5000, TimeUnit.MILLISECONDS)
                .build();
        CloseableHttpClient client = HttpClientBuilder
                .create()
                .setConnectionManager(poolingHttpClientConnectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();


        HttpComponentsClientHttpRequestFactory httpRequestFactory = new
                HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setHttpClient(client);
        return builder
//                .interceptors(SpringWebTelemetry.create(openTelemetry).newInterceptor())
                .requestFactory(() -> httpRequestFactory)
                .build();
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return customizer -> {
//            customizer.featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
//            customizer.featuresToDisable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        };
    }

}
