package tech.duo.app.rest;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.cassandra.core.AsyncCassandraOperations;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import tech.duo.app.cassandra.AccountEntity;
import tech.duo.app.cassandra.AccountEntityRepository;
import tech.duo.app.http.ApiClient;
import tech.duo.app.http.Responses;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Optional.of;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public abstract class AccountController {
    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private static final Executor newCachedThreadPool = Executors.newCachedThreadPool();
    private final ExecutorService virtualThreadPerTaskExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final ApiClient apiClient;
    private final AccountEntityRepository accountEntityRepository;
    private final AsyncCassandraOperations cassandraOperations;

    public AccountController(ApiClient apiClient, AccountEntityRepository accountEntityRepository,
                             AsyncCassandraOperations cassandraOperations) {
        this.apiClient = apiClient;
        this.accountEntityRepository = accountEntityRepository;
        this.cassandraOperations = cassandraOperations;
    }

    @GetMapping("/accounts-spring-data")
    public List<SimpleAccountDto> getAccounts() {
        return accountEntityRepository.findAll()
                .stream()
                .map(account -> new SimpleAccountDto(account.getId(), account.getName()))
                .toList();
    }

    @GetMapping("/accounts-spring-data-async")
    public CompletableFuture<List<SimpleAccountDto>> getAccountsAsync() {
        return accountEntityRepository.findAllCompletableFuture()
                .thenApply(accounts -> accounts
                        .stream()
                        .map(account -> new SimpleAccountDto(account.getId(), account.getName()))
                        .toList());
    }

    /**
     * Call another API to get the balance and then query the database to aggregate everything to one response.
     * Metrics from perf tests <a href="http://localhost:3000/d/isnCArvnz/jvm-overview?orgId=1&from=1730447007110&to=1730447642238">here</a>
     */
    @GetMapping("/accounts-async-cql-template-before/{id}")
    public CompletableFuture<AccountWithBalanceResponse> getAccountByIdBefore(@PathVariable String id) {

        var balance = apiClient.getBalance(id);
        return cassandraOperations.selectOneById(id, AccountEntity.class)
                .thenApply(account -> new AccountWithBalanceResponse(id, account.getName(),
                        balance.balance().currency(), balance.balance().amount()));
    }

    /**
     * Query the database and use the returned {@link tech.duo.app.http.Responses.Account}#id to get the balance from another API and then aggregate everything into one response.
     * Metrics from perf tests <a href="http://localhost:3000/d/isnCArvnz/jvm-overview?orgId=1&from=1730447703050&to=1730448337816">here</a>
     */
    @GetMapping("/accounts-async-cql-template-after/{id}")
    public CompletableFuture<AccountWithBalanceResponse> getAccountById(@PathVariable String id) {

        return cassandraOperations.selectOneById(id, AccountEntity.class)
                .thenApply(account -> {
                    var balance = apiClient.getBalance(account.getId());
                    return new AccountWithBalanceResponse(account.getId(), account.getName(),
                            balance.balance().currency(), balance.balance().amount());
                });
    }

    @GetMapping("/accounts-spring-data-cf-then-apply/{id}")
    public CompletableFuture<AccountWithBalanceResponse> accountWithBalance2(@PathVariable String id) {
        return accountEntityRepository.findAllById(id)
                .thenApply(account -> {
                    var balance = apiClient.getBalance(account.getId());
                    return new AccountWithBalanceResponse(account.getId(), account.getName(),
                            balance.balance().currency(), balance.balance().amount());
                });
    }

    /**
     * Metrics <a href="http://localhost:3000/d/isnCArvnz/jvm-overview?orgId=1&from=1730448817144&to=1730451501512">here</a>
     */
    @GetMapping("/accounts-spring-data-cf-then-apply-async/{id}")
    public CompletableFuture<AccountWithBalanceResponse> accountWithBalance3(@PathVariable String id) {
        return accountEntityRepository.findAllById(id)
                .thenApplyAsync(account -> {
                    var balance = apiClient.getBalance(account.getId());
                    return new AccountWithBalanceResponse(account.getId(), account.getName(),
                            balance.balance().currency(), balance.balance().amount());
                });
    }

    @GetMapping("/accounts-spring-data-cf-then-apply-async-vt/{id}")
    public CompletableFuture<AccountWithBalanceResponse> accountWithBalance5(@PathVariable String id) {
        return accountEntityRepository.findAllById(id)
                .thenApplyAsync(account -> {
                    var balance = apiClient.getBalance(account.getId());
                    return new AccountWithBalanceResponse(account.getId(), account.getName(),
                            balance.balance().currency(), balance.balance().amount());
                }, virtualThreadPerTaskExecutor);
    }

    @GetMapping("/accounts-spring-data-cf-then-apply-async-cached-tp/{id}")
    public CompletableFuture<AccountWithBalanceResponse> accountWithBalance6(@PathVariable String id) {
        return accountEntityRepository.findAllById(id)
                .thenApplyAsync(account -> {
                    var balance = apiClient.getBalance(account.getId());
                    return new AccountWithBalanceResponse(account.getId(), account.getName(),
                            balance.balance().currency(), balance.balance().amount());
                }, newCachedThreadPool);
    }

    @GetMapping("/accounts-spring-data-blocking/{id}")
    public AccountWithBalanceResponse accountWithBalance4(@PathVariable String id) {
        var account = accountEntityRepository.findById(id).orElseThrow();
        var balance = apiClient.getBalance(account.getId());
        return new AccountWithBalanceResponse(account.getId(), account.getName(),
                balance.balance().currency(), balance.balance().amount());
    }


    @GetMapping("/accounts-mutiple-calls-blocking")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AccountsResponse getRemote() {
        Responses.AccountResponse accounts = apiClient.getAccounts();

        var list = accounts.accounts()
                .stream()
                .map(account -> {

                    var balance = apiClient.getBalance(account.id());
                    return new AccountWithBalanceResponse(account.id(), account.name(),
                            balance.balance().currency(), balance.balance().amount());
                }).toList();

        return new AccountsResponse(list);
    }

    @GetMapping("/accounts-mutiple-calls-async-default")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CompletableFuture<AccountsResponse> getRemoteAsyncDefault() {
        return supplyAsync(() -> apiClient.getAccounts())
                .thenCompose(accounts -> {
                    var list = accounts.accounts()
                            .stream()
                            .map(account -> supplyAsync(() -> {
                                var balance = apiClient.getBalance(account.id());
                                return new AccountWithBalanceResponse(account.id(), account.name(),
                                        balance.balance().currency(), balance.balance().amount());
                            })).toList();

                    CompletableFuture<Void> all = CompletableFuture.allOf(list.toArray(new CompletableFuture[]{}));

                    return all.thenApply(v -> new AccountsResponse(list.stream()
                            .map(CompletableFuture::join)
                            .toList()));
                });
    }

    @GetMapping("/accounts-mutiple-calls-async-vt-supply-async")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CompletableFuture<AccountsResponse> getRemoteAsyncVtAsync() {
        return supplyAsync(() -> apiClient.getAccounts(), virtualThreadPerTaskExecutor)
                .thenCompose(accounts -> {

                    List<CompletableFuture<AccountWithBalanceResponse>> list = accounts.accounts()
                            .stream()
                            .map(account -> supplyAsync(() -> {
                                var balance = apiClient.getBalance(account.id());
                                return new AccountWithBalanceResponse(account.id(), account.name(),
                                        balance.balance().currency(), balance.balance().amount());
                            }, virtualThreadPerTaskExecutor)).toList();

                    CompletableFuture<Void> all = CompletableFuture.allOf(list.toArray(new CompletableFuture[]{}));

                    return all.thenApply(v -> new AccountsResponse(list.stream()
                            .map(CompletableFuture::join)
                            .toList()));
                });
    }

    @GetMapping("/accounts-mutiple-calls-async-vt")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AccountsResponse getRemoteAsyncVt() {
        var accounts = apiClient.getAccounts();

        var futures = accounts.accounts()
                .stream()
                .map(account -> supplyAsync(() -> {
                    var balance = apiClient.getBalance(account.id());
                    return new AccountWithBalanceResponse(account.id(), account.name(),
                            balance.balance().currency(), balance.balance().amount());
                }, virtualThreadPerTaskExecutor))
                .toList();


        return new AccountsResponse(
                futures.stream()
                        .map(CompletableFuture::join)
                        .toList());
    }

    @GetMapping("/admin/synchronization/accounts")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AccountsResponse sync() throws ExecutionException, InterruptedException {
        Responses.AccountResponse accounts = apiClient.getAccounts();

        var list = accounts.accounts()
                .stream()
                .map(account -> {

                    accountEntityRepository.findById(account.id())
                            .or(() -> {
                                var accountEntity = new AccountEntity();
                                accountEntity.setId(account.id());
                                accountEntity.setName(account.name());
                                return of(accountEntityRepository.save(accountEntity));
                            });

                    var balance = apiClient.getBalance(account.id());
                    return new AccountWithBalanceResponse(account.id(), account.name(),
                            balance.balance().currency(), balance.balance().amount());
                }).toList();

        return new AccountsResponse(list);
    }


    public record SimpleAccountDto(String id, String name) {
    }

    public record AccountWithBalanceResponse(String id, String name, String currency, BigDecimal amount) {
    }

    public record AccountsResponse(List<AccountWithBalanceResponse> accounts) {
    }
}
