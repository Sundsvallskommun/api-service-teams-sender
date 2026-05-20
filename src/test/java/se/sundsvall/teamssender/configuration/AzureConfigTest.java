package se.sundsvall.teamssender.configuration;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

class AzureConfigTest {

	@Test
	void testAzureBean() {
		assertThat(AzureConfig.Azure.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters()));
	}

	@Test
	void azureConfigGetterAndSetter() {
		final var config = new AzureConfig();
		final var azure = new AzureConfig.Azure();
		azure.setTenantId("tenant");
		azure.setClientId("client");
		azure.setClientSecret("secret");
		config.setAd(Map.of("2281", azure));

		assertThat(config.getAd()).containsEntry("2281", azure);
		assertThat(azure.getTenantId()).isEqualTo("tenant");
		assertThat(azure.getClientId()).isEqualTo("client");
		assertThat(azure.getClientSecret()).isEqualTo("secret");
	}
}
