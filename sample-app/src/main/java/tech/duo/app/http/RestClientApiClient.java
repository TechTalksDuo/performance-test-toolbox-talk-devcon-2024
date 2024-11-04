package tech.duo.app.http;

import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.JettyClientHttpRequestFactory;
import org.springframework.http.client.ReactorNettyClientRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Service
public class RestClientApiClient implements ApiClient {
    private static final Logger log = LoggerFactory.getLogger(RestClientApiClient.class);

    private final RestClient restClient;

    public RestClientApiClient(
            @Value("${rest-template.connections.max-total:200}")
            int maxTotal,
            @Value("${wiremock.server.hostname:127.0.0.1}") String host,
                               @Value("${wiremock.server.port:18080}") int port,
                               ObservationRegistry collectorRegistry) {
        HttpClient client = HttpClient.create(ConnectionProvider.builder("custom")
                .maxIdleTime(Duration.ofSeconds(30))
                .maxConnections(maxTotal)
                .pendingAcquireMaxCount(maxTotal)
                .build());
        ReactorNettyClientRequestFactory requestFactory = new ReactorNettyClientRequestFactory(client);
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
//                .requestFactory(new HttpComponentsClientHttpRequestFactory())
//                .requestFactory(new JettyClientHttpRequestFactory())
                .baseUrl("http://%s:%d/api".formatted(host, port))
                .observationRegistry(collectorRegistry)
                .build();
//        HttpClient h= HttpClient.newBuilder()
//                .build();
//        HttpResponse<Responses.AccountResponse> s = h.send(HttpRequest.newBuilder()
//                        .uri(URI.create(""))
//                .GET()
//                .build(), HttpResponse.BodyHandlers.ofString());
    }

    @Override
    public Responses.UserResponse getUser() {
        log.debug("getUser");

        Responses.UserResponse res = restClient.get()
                .uri("/users")
                .retrieve()
                .body(Responses.UserResponse.class);
        log.debug("getUser - got response");
        return res;
    }

    @Override
    public Responses.AccountResponse getAccounts() {
        log.debug("getAccounts");

        return restClient.get()
                .uri("/accounts")
                .retrieve()
                .body(Responses.AccountResponse.class);
    }

    @Override
    public Responses.BalanceResponse getBalance(String accountId) {
        log.debug("getBalance - accountId:{}, thread: {}", accountId, Thread.currentThread());

        return restClient.get()
                .uri("/accounts/{accountId}/balance", accountId)
                .retrieve()
                .body(Responses.BalanceResponse.class);
    }
}
