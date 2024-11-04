package tech.duo.app.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AccountEntityRepository extends CassandraRepository<AccountEntity, String> {

    @Async
    CompletableFuture<AccountEntity> findAllById(String id);

    @Query("select * from accountentity")
    @Async
    CompletableFuture<List<AccountEntity>> findAllCompletableFuture();
}
