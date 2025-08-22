package se.sundsvall.teamssender.auth.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import org.junit.jupiter.api.Test;

class StaticTokenCredentialTest {

	@Test
	void getToken_shouldReturnProvidedToken() {
		String expectedToken = "fake-access-token";
		StaticTokenCredential credential = new StaticTokenCredential(expectedToken);

		// Blockera Mono och få token direkt
		AccessToken token = credential.getToken(new TokenRequestContext()).block();

		assertNotNull(token);
		assertEquals(expectedToken, token.getToken());
		assertTrue(token.getExpiresAt().isAfter(java.time.OffsetDateTime.now()));
	}
}
