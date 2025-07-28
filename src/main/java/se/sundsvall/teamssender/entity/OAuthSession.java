package se.sundsvall.teamssender.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "oauth_sessions")
public class OAuthSession {

	@Id
	@Column(nullable = false, unique = true)
	private String userId;

	@Column(length = 2048)
	private String authorizationCode;

	@Column(columnDefinition = "TEXT")
	private String accessToken;

	@Column(columnDefinition = "TEXT")
	private String refreshToken;

	@Column(nullable = false)
	private Instant expiresAt;

	// --- Constructors ---

	public OAuthSession() {}

	public OAuthSession(String userId, String authorizationCode) {
		this.userId = userId;
		this.authorizationCode = authorizationCode;
	}

	// --- Getters & Setters ---

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAuthorizationCode() {
		return authorizationCode;
	}

	public void setAuthorizationCode(String authorizationCode) {
		this.authorizationCode = authorizationCode;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	public Instant getExpiresAt() {
		return expiresAt;
	}
	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}
}