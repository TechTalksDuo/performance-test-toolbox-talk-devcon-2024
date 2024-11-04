package tech.duo.app.http;

import java.math.BigDecimal;
import java.util.List;

public class Responses {


    public record AccountResponse(List<Account> accounts) {
    }

    public record UserResponse(User user) {
        public record User(String name) {

        }
    }
    public record Account(String id, String name) {

    }

    public record BalanceResponse(Balance balance) {

    }

    public record Balance(String currency, BigDecimal amount) {
    }
}
