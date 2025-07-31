package se.sundsvall.teamssender.auth.repository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.teamssender.auth.model.TokenCacheEntity;

@CircuitBreaker(name = "tokenCacheRepository")
public interface ITokenCacheRepository extends JpaRepository<TokenCacheEntity, String> {
}
