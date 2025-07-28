package se.sundsvall.teamssender.auth.repository;

import java.util.Optional;

public interface ITokenCacheRepository {

	Optional<byte[]> load(String userId);

	void save(String userId, byte[] cacheData);
}
