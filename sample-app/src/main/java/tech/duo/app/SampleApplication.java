package tech.duo.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Thread switching overhead visible below:
 * <ul>
 *     <li>With @{@link EnableAsync} <a href="http://localhost:3000/d/isnCArvnz/jvm-overview?orgId=1&from=1730196744535&to=1730197445599">metrics</a></li>
 *     <li>Without @{@link EnableAsync} <a href="http://localhost:3000/d/isnCArvnz/jvm-overview?orgId=1&from=1730194824800&to=1730195532627">metrics</a></li>
 * </ul>
 *
 */
@EnableAsync
@SpringBootApplication
public class SampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }

}
