package se.sundsvall.teamssender.integration.db;

import com.microsoft.aad.msal4j.ITokenCache;
import com.microsoft.aad.msal4j.ITokenCacheAccessContext;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.teamssender.integration.db.model.TokenCacheEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseTokenCacheTest {

	private static final String USER_ID = "user@example.com";
	private static final String SERIALIZED_CACHE = "{\"AccessToken\":{}}";

	@Mock
	private TokenCacheRepository repositoryMock;

	@Mock
	private ITokenCacheAccessContext contextMock;

	@Mock
	private ITokenCache tokenCacheMock;

	private DatabaseTokenCache cache;

	@BeforeEach
	void setUp() {
		cache = new DatabaseTokenCache(USER_ID, repositoryMock);
	}

	@Test
	void beforeCacheAccessWithExistingRowDeserializesIntoMsalCache() {
		final var entity = TokenCacheEntity.create()
			.withUserId(USER_ID)
			.withCacheData(SERIALIZED_CACHE.getBytes(StandardCharsets.UTF_8));
		when(repositoryMock.findById(USER_ID)).thenReturn(Optional.of(entity));
		when(contextMock.tokenCache()).thenReturn(tokenCacheMock);

		cache.beforeCacheAccess(contextMock);

		verify(tokenCacheMock).deserialize(SERIALIZED_CACHE);
	}

	@Test
	void beforeCacheAccessWithNoRowDoesNothing() {
		when(repositoryMock.findById(USER_ID)).thenReturn(Optional.empty());

		cache.beforeCacheAccess(contextMock);

		verifyNoInteractions(contextMock);
	}

	@Test
	void afterCacheAccessSavesWhenCacheChanged() {
		when(contextMock.hasCacheChanged()).thenReturn(true);
		when(contextMock.tokenCache()).thenReturn(tokenCacheMock);
		when(tokenCacheMock.serialize()).thenReturn(SERIALIZED_CACHE);

		cache.afterCacheAccess(contextMock);

		final var captor = ArgumentCaptor.forClass(TokenCacheEntity.class);
		verify(repositoryMock).save(captor.capture());
		assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
		assertThat(captor.getValue().getCacheData()).isEqualTo(SERIALIZED_CACHE.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	void afterCacheAccessSkipsSaveWhenCacheUnchanged() {
		when(contextMock.hasCacheChanged()).thenReturn(false);

		cache.afterCacheAccess(contextMock);

		verify(repositoryMock, never()).save(any());
		verifyNoInteractions(tokenCacheMock);
	}
}
