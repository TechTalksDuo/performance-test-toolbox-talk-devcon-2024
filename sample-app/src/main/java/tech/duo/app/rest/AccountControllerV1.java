package tech.duo.app.rest;


import org.springframework.data.cassandra.core.AsyncCassandraOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.duo.app.cassandra.AccountEntityRepository;
import tech.duo.app.http.RestTemplateApiClient;

@RequestMapping("/api/v1")
@RestController
public class AccountControllerV1 extends AccountController {

    public AccountControllerV1(RestTemplateApiClient apiClient, AccountEntityRepository accountEntityRepository,
                               AsyncCassandraOperations cassandraOperations) {
        super(apiClient, accountEntityRepository, cassandraOperations);
    }

}
