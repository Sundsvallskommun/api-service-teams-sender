package se.sundsvall.teamssender.configuration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AzureConfigTest {

	@Test
	void testAzureConfigAndNestedAzure() {
		AzureConfig config = new AzureConfig();
		AzureConfig.Azure azure = new AzureConfig.Azure();

		azure.setUser("user1");
		azure.setClientId("clientId1");
		azure.setTenantId("tenantId1");
		azure.setRedirectUri("http://localhost/redirect");
		azure.setScopes("scope1 scope2");
		azure.setAuthorityUrl("https://login.microsoftonline.com");
		azure.setClientSecret("secret");
		azure.setLoginUrl("https://login.url");

		Map<String, AzureConfig.Azure> adMap = new HashMap<>();
		adMap.put("tenant1", azure);
		config.setAd(adMap);

		assertNotNull(config.getAd());
		assertEquals(1, config.getAd().size());
		assertEquals("clientId1", config.getAd().get("tenant1").getClientId());
		assertEquals("user1", config.getAd().get("tenant1").getUser());
	}
}
