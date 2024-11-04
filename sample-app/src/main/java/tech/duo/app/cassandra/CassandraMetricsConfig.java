package tech.duo.app.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.boot.autoconfigure.cassandra.DriverConfigLoaderBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.AsyncCassandraOperations;
import org.springframework.data.cassandra.core.AsyncCassandraTemplate;

import java.util.List;

import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.METRICS_FACTORY_CLASS;
import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.METRICS_NODE_ENABLED;
import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.METRICS_SESSION_ENABLED;

@Configuration
public class CassandraMetricsConfig {


    @Bean
    public AsyncCassandraOperations cassandraOperations(CqlSession session) {
        return new AsyncCassandraTemplate(session);
    }

    @Bean
    public DriverConfigLoaderBuilderCustomizer driverConfigLoaderBuilderCustomizer() {
        return builder -> {
            builder.withString(METRICS_FACTORY_CLASS, "com.datastax.oss.driver.internal.metrics.micrometer.MicrometerMetricsFactory");
            builder.withStringList(METRICS_SESSION_ENABLED, List.of("connected-nodes", "cql-requests"));
            builder.withStringList(METRICS_NODE_ENABLED, List.of("pool.open-connections", "pool.in-flight"));
        };
    }
}
