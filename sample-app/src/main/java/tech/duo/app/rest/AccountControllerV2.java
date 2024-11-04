package tech.duo.app.rest;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.cassandra.core.AsyncCassandraOperations;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tech.duo.app.cassandra.AccountEntity;
import tech.duo.app.cassandra.AccountEntityRepository;
import tech.duo.app.http.ApiClient;
import tech.duo.app.http.Responses;
import tech.duo.app.http.RestClientApiClient;
import tech.duo.app.http.RestTemplateApiClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Optional.of;

@RequestMapping("/api/v2")
@RestController
public class AccountControllerV2 extends AccountController {

    public AccountControllerV2(RestClientApiClient apiClient, AccountEntityRepository accountEntityRepository,
                               AsyncCassandraOperations cassandraOperations) {
        super(apiClient, accountEntityRepository, cassandraOperations);
    }


}
