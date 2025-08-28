package se.sundsvall.teamssender.auth.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TokenCacheEntityValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setup() {
		try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
			validator = factory.getValidator();
		}
	}

	@Test
	void testUserIdNotNull() {
		TokenCacheEntity entity = new TokenCacheEntity(null, "data".getBytes());

		Set<ConstraintViolation<TokenCacheEntity>> violations = validator.validate(entity);
		assertFalse(violations.isEmpty(), "userId should not be null");
	}

	@Test
	void testCacheDataNotNull() {
		TokenCacheEntity entity = new TokenCacheEntity("user123", null);

		Set<ConstraintViolation<TokenCacheEntity>> violations = validator.validate(entity);
		assertFalse(violations.isEmpty(), "cacheData should not be null");
	}

	@Test
	void testValidEntity() {
		TokenCacheEntity entity = new TokenCacheEntity("user123", "someCacheData".getBytes());

		Set<ConstraintViolation<TokenCacheEntity>> violations = validator.validate(entity);
		assertTrue(violations.isEmpty(), "Entity should be valid");
	}
}
