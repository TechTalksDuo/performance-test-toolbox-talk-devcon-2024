package tech.duo.app;

import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.ForkJoinPool;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class ResourcesLogger implements CommandLineRunner {
    private static final Logger log = getLogger(ResourcesLogger.class);

    @Override
    public void run(String... args) throws Exception {
        log.warn("run - availableProcessors: {}", Runtime.getRuntime().availableProcessors());
        log.warn("run - maxMemory: {}", Runtime.getRuntime().maxMemory());
        log.warn("run - commonPool.getParallelism: {}", ForkJoinPool.commonPool().getParallelism());
    }
}
