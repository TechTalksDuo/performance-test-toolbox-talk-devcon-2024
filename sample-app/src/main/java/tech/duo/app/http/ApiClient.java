package tech.duo.app.http;

public interface ApiClient {

    Responses.UserResponse getUser();

    Responses.AccountResponse getAccounts();

    Responses.BalanceResponse getBalance(String accountId);
}
