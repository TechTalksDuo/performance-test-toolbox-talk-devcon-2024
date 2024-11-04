package tech.duo.app.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RestTemplateApiClient implements ApiClient {
    private static final Logger log = LoggerFactory.getLogger(RestTemplateApiClient.class);
    private final RestTemplate restTemplate;

    private final String host;
    private final int port;

    public RestTemplateApiClient(@Value("${wiremock.server.hostname:127.0.0.1}") String host, @Value("${wiremock.server.port:18080}") int port, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.host = host;
        this.port = port;
        log.info("BlockingApiClient - calling remote server at: {}:{}", host, port);
    }

    @Override
    public Responses.UserResponse getUser() {
        log.debug("getUser");

        ResponseEntity<Responses.UserResponse> res = restTemplate.getForEntity("http://%s:%d/api/users".formatted(host, port), Responses.UserResponse.class);
        log.debug("getUser - got response");
        if (res.getStatusCode().isError()) {
            log.warn("getUser - got error: " + res);
            throw new IllegalStateException("could not get user. received status: " + res.getStatusCode());
        }
        return res.getBody();
    }

    @Override
    public Responses.AccountResponse getAccounts() {
        log.debug("getAccounts");

        ResponseEntity<Responses.AccountResponse> res = restTemplate.getForEntity("http://%s:%d/api/accounts".formatted(host, port), Responses.AccountResponse.class);
        log.debug("getAccounts - got response");
        if (res.getStatusCode().isError()) {
            log.warn("getAccounts - got error: " + res);
            throw new IllegalStateException("could not get account. received status: " + res.getStatusCode());
        }
        return res.getBody();
    }

    @Override
    public Responses.BalanceResponse getBalance(String accountId) {
        log.debug("getBalance - accountId:{}, thread: {}", accountId, Thread.currentThread());

        ResponseEntity<Responses.BalanceResponse> res = restTemplate.getForEntity("http://%s:%d/api/accounts/%s/balance".formatted(host, port, accountId), Responses.BalanceResponse.class);
        log.debug("getBalance - got response for accountId: {}", accountId);
        if (res.getStatusCode().isError()) {
            throw new IllegalStateException("could not get account. received status: " + res.getStatusCode());
        }
        return res.getBody();
    }
}
