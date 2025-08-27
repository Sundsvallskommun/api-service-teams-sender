package se.sundsvall.teamssender.auth.model;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Timestamp;
import org.junit.jupiter.api.Test;

class TokenCacheEntityTest {

	@Test
	void testNoArgsConstructorAndSetters() {
		byte[] data = {
			1, 2, 3
		};
		TokenCacheEntity entity = new TokenCacheEntity();
		entity.setUserId("user1");
		entity.setCacheData(data);

		assertEquals("user1", entity.getUserId());
		assertArrayEquals(data, entity.getCacheData());

		assertNull(entity.getLastUpdated());
	}

	@Test
	void testAllArgsConstructor() {
		byte[] data = {
			4, 5, 6
		};
		TokenCacheEntity entity = new TokenCacheEntity("user2", data);

		assertEquals("user2", entity.getUserId());
		assertArrayEquals(data, entity.getCacheData());

		assertNull(entity.getLastUpdated());
	}

	@Test
	void testLastUpdatedSetterAndGetter() {
		TokenCacheEntity entity = new TokenCacheEntity();
		Timestamp now = new Timestamp(System.currentTimeMillis());
		entity.setLastUpdated(now);

		assertEquals(now, entity.getLastUpdated());
	}
}
