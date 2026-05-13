package se.sundsvall.teamssender.integration.db;

import com.microsoft.aad.msal4j.ITokenCacheAccessAspect;
import com.microsoft.aad.msal4j.ITokenCacheAccessContext;
import java.nio.charset.StandardCharsets;
import se.sundsvall.teamssender.integration.db.model.TokenCacheEntity;

public class DatabaseTokenCache implements ITokenCacheAccessAspect {

	private final String userId;
	private final TokenCacheRepository tokenCacheRepository;

	public DatabaseTokenCache(final String userId, final TokenCacheRepository tokenCacheRepository) {
		this.userId = userId;
		this.tokenCacheRepository = tokenCacheRepository;
	}

	@Override
	public void beforeCacheAccess(final ITokenCacheAccessContext context) {
		tokenCacheRepository.findById(userId)
			.map(TokenCacheEntity::getCacheData)
			.ifPresent(cacheData -> {
				final String cacheString = new String(cacheData, StandardCharsets.UTF_8);
				context.tokenCache().deserialize(cacheString);
			});
	}

	@Override
	public void afterCacheAccess(final ITokenCacheAccessContext context) {
		if (context.hasCacheChanged()) {
			final String serializedCache = context.tokenCache().serialize();
			final byte[] cacheData = serializedCache.getBytes(StandardCharsets.UTF_8);
			tokenCacheRepository.save(TokenCacheEntity.create().withUserId(userId).withCacheData(cacheData));
		}
	}
}
