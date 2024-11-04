package tech.duo.app.rest;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.duo.app.http.RestTemplateApiClient;

@RestController
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final RestTemplateApiClient apiClient;

    public UserController(RestTemplateApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @GetMapping("/api/users")
    public UserResponse getUserRestTemplate() {
        var user = apiClient.getUser();
        return new UserResponse(user.user().name());
    }

    record UserResponse(String name) {
    }

}
