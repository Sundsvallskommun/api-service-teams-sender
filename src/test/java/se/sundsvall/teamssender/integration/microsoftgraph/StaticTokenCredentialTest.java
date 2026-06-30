package se.sundsvall.teamssender.integration.microsoftgraph;

import com.azure.core.credential.TokenRequestContext;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StaticTokenCredentialTest {

	@Test
	void getTokenReturnsTheStaticAccessToken() {
		final var credential = new StaticTokenCredential("my-access-token");

		final var token = credential.getToken(new TokenRequestContext()).block();

		assertThat(token).isNotNull();
		assertThat(token.getToken()).isEqualTo("my-access-token");
		assertThat(token.getExpiresAt()).isAfter(OffsetDateTime.now());
	}
}
