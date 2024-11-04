package tech.duo.app.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.java11.instrument.binder.jdk.MicrometerHttpClient;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class HttpClientApiClient implements ApiClient {
    private static final Logger log = LoggerFactory.getLogger(HttpClientApiClient.class);

    private final HttpClient restClient;
    private final ObjectMapper mapper;
    private final String host;
    private final int port;

    public HttpClientApiClient(@Value("${wiremock.server.hostname:127.0.0.1}") String host,
                               @Value("${wiremock.server.port:18080}") int port,
                               MeterRegistry meterRegistry,
                               ObservationRegistry observationRegistry, ObjectMapper mapper) {
        this.mapper = mapper;
        this.host = host;
        this.port = port;

        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        restClient = MicrometerHttpClient.instrumentationBuilder(httpClient, meterRegistry)
                .observationRegistry(observationRegistry)
                .build();

    }

    @Override
    public Responses.UserResponse getUser() {
        log.debug("getUser");
        try {
            var s = restClient.send(HttpRequest.newBuilder()
                    .uri(URI.create("http://%s:%d/api/users".formatted(host, port)))
                    .GET()
                    .build(), HttpResponse.BodyHandlers.ofString());
            if (s.statusCode() != 200) {
                throw new RuntimeException("Failed call with status: " + s.statusCode());
            }
            return mapper.readValue(s.body(), Responses.UserResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Responses.AccountResponse getAccounts() {
        log.debug("getAccounts");

        try {
            var s = restClient.send(HttpRequest.newBuilder()
                    .uri(URI.create("http://%s:%d/api/accounts".formatted(host, port)))
                    .GET()
                    .build(), HttpResponse.BodyHandlers.ofString());
            if (s.statusCode() != 200) {
                throw new RuntimeException("Failed call with status: " + s.statusCode());
            }
            return mapper.readValue(s.body(), Responses.AccountResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Responses.BalanceResponse getBalance(String accountId) {
        log.debug("getBalance - accountId:{}, thread: {}", accountId, Thread.currentThread());

        try {
            var s = restClient.send(HttpRequest.newBuilder()
                    .uri(URI.create("http://%s:%d/api/accounts/%s/balance".formatted(host, port, accountId)))
                    .GET()
                    .build(), HttpResponse.BodyHandlers.ofString());
            if (s.statusCode() != 200) {
                throw new RuntimeException("Failed call with status: " + s.statusCode());
            }
            return mapper.readValue(s.body(), Responses.BalanceResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
