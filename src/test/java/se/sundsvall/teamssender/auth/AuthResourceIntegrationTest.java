package se.sundsvall.teamssender.auth;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import se.sundsvall.teamssender.auth.service.TokenService;
import se.sundsvall.teamssender.configuration.AzureConfig;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthResource.class)
class AuthResourceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private TokenService tokenService;

    @Mock
    private AzureConfig azureConfig;

    @TestConfiguration
    static class MockConfig {
        @Bean
        TokenService tokenService(@Mock TokenService tokenService) {
            return tokenService;
        }

        @Bean
        AzureConfig azureConfig(@Mock AzureConfig azureConfig) {
            return azureConfig;
        }
    }
    @Test
    void login_validMunicipality_returnsRedirect() throws Exception {
        var mockAzure = mock(AzureConfig.Azure.class);
        when(mockAzure.getLoginUrl()).thenReturn("https://login.microsoftonline.com/test");

        var adMap = Map.of("2281", mockAzure);
        when(azureConfig.getAd()).thenReturn(adMap);

        mockMvc.perform(get("/2281/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://login.microsoftonline.com/test"));
    }

    @Test
    void login_invalidMunicipality_returns404() throws Exception {
        // Tom map, dvs ingen kommun är giltig
        when(azureConfig.getAd()).thenReturn(Map.of());

        mockMvc.perform(get("/9999/login"))
                .andExpect(status().isNotFound());
    }

    @Test
    void callback_returnsExpectedResponseFromTokenService() throws Exception {
        when(tokenService.exchangeAuthCodeForToken("abc123", "2281"))
                .thenReturn(org.springframework.http.ResponseEntity.ok("token-data"));

        mockMvc.perform(get("/callback")
                        .param("code", "abc123")
                        .param("state", "2281"))
                .andExpect(status().isOk())
                .andExpect(content().string("token-data"));
    }
}
