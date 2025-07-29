//package se.sundsvall.teamssender.scheduled;
//
//import java.time.Instant;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import se.sundsvall.teamssender.repository.OAuthSessionRepository;
//import se.sundsvall.teamssender.service.TokenService;
//
//@Component
//public class TokenRefresher {
//
//	private final OAuthSessionRepository repo;
//	private final TokenService tokenService;
//
//	public TokenRefresher(OAuthSessionRepository repo, TokenService tokenService) {
//		this.repo = repo;
//		this.tokenService = tokenService;
//	}
//
//	@Scheduled(fixedDelay = 60000)
//	public void proactiveRefresh() {
//		System.out.println("Proactive Refresh");
//		repo.findAll().forEach(session -> {
//			if (Instant.now().isAfter(session.getExpiresAt().minusSeconds(300))) {
//				tokenService.getValidAccessToken(session.getUserId());
//			}
//		});
//	}
//}
