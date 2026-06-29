package se.sundsvall.teamssender.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.teamssender.integration.db.model.TokenCacheEntity;

@CircuitBreaker(name = "tokenCacheRepository")
public interface TokenCacheRepository extends JpaRepository<TokenCacheEntity, String> {
}
