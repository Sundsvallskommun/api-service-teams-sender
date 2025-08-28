package se.sundsvall.teamssender.auth.integration;

import static org.mockito.Mockito.*;

import com.microsoft.aad.msal4j.ITokenCache;
import com.microsoft.aad.msal4j.ITokenCacheAccessContext;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.teamssender.auth.model.TokenCacheEntity;
import se.sundsvall.teamssender.auth.repository.ITokenCacheRepository;

@ExtendWith(MockitoExtension.class)
class DatabaseTokenCacheTest {

	private static final String USER_ID = "user1";

	private DatabaseTokenCache databaseTokenCache;

	@Mock
	private ITokenCacheRepository tokenCacheRepository;

	@Mock
	private ITokenCacheAccessContext context;

	@Mock
	private ITokenCache tokenCache;

	@BeforeEach
	void setup() {
		databaseTokenCache = new DatabaseTokenCache(USER_ID, tokenCacheRepository);
	}

	@Test
	void beforeCacheAccess_shouldDeserializeCacheIfPresent() {
		byte[] fakeCache = "cached-data".getBytes(StandardCharsets.UTF_8);
		TokenCacheEntity entity = new TokenCacheEntity(USER_ID, fakeCache);

		when(tokenCacheRepository.findById(USER_ID)).thenReturn(Optional.of(entity));
		when(context.tokenCache()).thenReturn(tokenCache);

		databaseTokenCache.beforeCacheAccess(context);

		verify(context).tokenCache();
		verify(tokenCache).deserialize("cached-data");
	}

	@Test
	void beforeCacheAccess_shouldDoNothingIfCacheNotPresent() {
		when(tokenCacheRepository.findById(USER_ID)).thenReturn(Optional.empty());

		databaseTokenCache.beforeCacheAccess(context);

		verify(context, never()).tokenCache();
		verify(tokenCache, never()).deserialize(any());
	}

	@Test
	void afterCacheAccess_shouldSaveCacheIfChanged() {
		when(context.hasCacheChanged()).thenReturn(true);
		when(context.tokenCache()).thenReturn(tokenCache);
		when(tokenCache.serialize()).thenReturn("serialized-data");

		databaseTokenCache.afterCacheAccess(context);

		verify(tokenCacheRepository).save(argThat(entity -> entity.getUserId().equals(USER_ID) &&
			new String(entity.getCacheData(), StandardCharsets.UTF_8).equals("serialized-data")));
	}

	@Test
	void afterCacheAccess_shouldDoNothingIfNotChanged() {
		when(context.hasCacheChanged()).thenReturn(false);

		databaseTokenCache.afterCacheAccess(context);

		verify(tokenCacheRepository, never()).save(any());
	}
}
