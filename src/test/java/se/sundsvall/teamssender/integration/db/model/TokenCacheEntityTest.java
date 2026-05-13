package se.sundsvall.teamssender.integration.db.model;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

class TokenCacheEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> Timestamp.from(Instant.now().plusSeconds(new Random().nextInt(1_000_000))), Timestamp.class);
	}

	@Test
	void testBean() {
		assertThat(TokenCacheEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var userId = "user@example.com";
		final var cacheData = "cached".getBytes();
		final var lastUpdated = Timestamp.from(Instant.now());

		final var bean = TokenCacheEntity.create()
			.withUserId(userId)
			.withCacheData(cacheData)
			.withLastUpdated(lastUpdated);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getUserId()).isEqualTo(userId);
		assertThat(bean.getCacheData()).isEqualTo(cacheData);
		assertThat(bean.getLastUpdated()).isEqualTo(lastUpdated);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new TokenCacheEntity()).hasAllNullFieldsOrProperties();
		assertThat(TokenCacheEntity.create()).hasAllNullFieldsOrProperties();
	}
}
