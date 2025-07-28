package se.sundsvall.teamssender.auth.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import se.sundsvall.teamssender.auth.model.TokenCacheEntity;

import java.util.Optional;

@Repository
public class TokenCacheRepository implements ITokenCacheRepository {

    private final TokenCacheJpaRepository tokenCacheJpaRepository;

    @Autowired
    public TokenCacheRepository(TokenCacheJpaRepository tokenCacheJpaRepository) {
        this.tokenCacheJpaRepository = tokenCacheJpaRepository;
    }

    @Override
    public Optional<byte[]> load(String userId) {
        return tokenCacheJpaRepository.findById(userId).map(TokenCacheEntity::getCacheData);
    }

    @Override
    public void save(String userId, byte[] cacheData) {
        tokenCacheJpaRepository.save(new TokenCacheEntity(userId, cacheData));
    }
}

