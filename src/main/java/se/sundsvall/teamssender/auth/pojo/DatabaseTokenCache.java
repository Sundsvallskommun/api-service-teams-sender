package se.sundsvall.teamssender.auth.pojo;

import com.microsoft.aad.msal4j.ITokenCacheAccessAspect;
import com.microsoft.aad.msal4j.ITokenCacheAccessContext;
import java.nio.charset.StandardCharsets;
import se.sundsvall.teamssender.auth.repository.ITokenCacheRepository;

public class DatabaseTokenCache implements ITokenCacheAccessAspect {

	private final String userId;
	private final ITokenCacheRepository tokenCacheRepository;

	public DatabaseTokenCache(String userId, ITokenCacheRepository tokenCacheRepository) {
		this.userId = userId;
		this.tokenCacheRepository = tokenCacheRepository;
	}

	@Override
	public void beforeCacheAccess(ITokenCacheAccessContext context) {
		tokenCacheRepository.load(userId).ifPresent(cacheData -> {
			String cacheString = new String(cacheData, StandardCharsets.UTF_8);
			context.tokenCache().deserialize(cacheString);
		});
	}

	@Override
	public void afterCacheAccess(ITokenCacheAccessContext context) {
		if (context.hasCacheChanged()) {
			String serializedCache = context.tokenCache().serialize();
			byte[] cacheData = serializedCache.getBytes(StandardCharsets.UTF_8);
			tokenCacheRepository.save(userId, cacheData);
		}
	}
}
