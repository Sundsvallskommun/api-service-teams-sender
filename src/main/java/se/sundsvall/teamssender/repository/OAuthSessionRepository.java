package se.sundsvall.teamssender.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.sundsvall.teamssender.entity.OAuthSession;

public interface OAuthSessionRepository extends JpaRepository<OAuthSession, Long> {

	@Query("SELECT o FROM OAuthSession o WHERE LOWER(o.userId) = LOWER(:userId)")
	Optional<OAuthSession> findByUserIdIgnoreCase(@Param("userId") String userId);
}