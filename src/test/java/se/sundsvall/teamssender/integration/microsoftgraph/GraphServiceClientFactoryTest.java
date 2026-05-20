package se.sundsvall.teamssender.integration.microsoftgraph;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.teamssender.configuration.AzureConfig;
import se.sundsvall.teamssender.configuration.TeamsProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class GraphServiceClientFactoryTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String UNKNOWN_MUNICIPALITY_ID = "9999";

	@Mock
	private AzureConfig azureConfigMock;

	private TeamsProperties teamsProperties;
	private GraphServiceClientFactory factory;

	@BeforeEach
	void setUp() {
		teamsProperties = new TeamsProperties();
		// Leave graphBaseUrl unset (production behaviour) - SDK uses its default.
		factory = new GraphServiceClientFactory(azureConfigMock, teamsProperties);
	}

	@Test
	void clientIsCachedPerMunicipality() {
		when(azureConfigMock.getAd()).thenReturn(Map.of(MUNICIPALITY_ID, azure()));

		final var first = factory.forMunicipality(MUNICIPALITY_ID);
		final var second = factory.forMunicipality(MUNICIPALITY_ID);

		assertThat(first).isSameAs(second);
	}

	@Test
	void unknownMunicipalityThrowsNotFound() {
		when(azureConfigMock.getAd()).thenReturn(Map.of(MUNICIPALITY_ID, azure()));

		assertThatThrownBy(() -> factory.forMunicipality(UNKNOWN_MUNICIPALITY_ID))
			.isInstanceOfSatisfying(ThrowableProblem.class, p -> assertThat(p.getStatus()).isEqualTo(NOT_FOUND))
			.hasMessageContaining(UNKNOWN_MUNICIPALITY_ID);
	}

	@Test
	void nullAdMapThrowsNotFound() {
		when(azureConfigMock.getAd()).thenReturn(null);

		assertThatThrownBy(() -> factory.forMunicipality(MUNICIPALITY_ID))
			.isInstanceOfSatisfying(ThrowableProblem.class, p -> assertThat(p.getStatus()).isEqualTo(NOT_FOUND));
	}

	private static AzureConfig.Azure azure() {
		final var azure = new AzureConfig.Azure();
		azure.setTenantId("tenant-id");
		azure.setClientId("client-id");
		azure.setClientSecret("client-secret");
		return azure;
	}
}
