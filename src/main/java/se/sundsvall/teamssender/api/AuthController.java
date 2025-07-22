package se.sundsvall.teamssender.api;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.teamssender.service.TokenService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@RestController
@RequestMapping("/") //Lägg till "/auth" när det är klart
public class AuthController {
    @Value("${azure.ad.tenant-id}")
    private String tenantId;
    @Value("${azure.ad.client-id}")
    private String clientId;
    @Value ("${azure.ad.client-secret}")
    private String clientSecret;
    @Value ("${azure.ad.redirecturi}")
    private String redirectUri;


    private final TokenService tokenService; // injecta denna via konstruktor

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {


        String scopes = "offline_access User.Read Chat.ReadWrite api://" + clientId + "/access_as_user";


        String url = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/authorize" +
                "?client_id=" + clientId +
                "&response_type=code" +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&response_mode=query" +
                "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8) +
                "&state=12345";

        response.sendRedirect(url);
    }
    @GetMapping("/callback") //Byt till "/callback" när det är klart
    public ResponseEntity<String> callback(@RequestParam String code, @RequestParam String state) {
        try {
            // Byt authorization code mot access token och refresh token
            tokenService.exchangeAuthorizationCodeForToken(code);

            return ResponseEntity.ok("Login succeeded, tokens saved!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Login failed: " + e.getMessage());
        }
    }
