package se.sundsvall.teamssender.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.teamssender.auth.model.TokenCacheEntity;

public interface ITokenCacheRepository extends JpaRepository<TokenCacheEntity, String> {
}
