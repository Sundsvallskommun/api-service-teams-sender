package se.sundsvall.teamssender.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.teamssender.entity.OAuthSession;

public interface OAuthSessionRepository extends JpaRepository<OAuthSession, Long> {
	Optional<OAuthSession> findByUserId(String userId);
}
