package se.sundsvall.teamssender.service;

import com.microsoft.graph.serviceclient.IAuthenticationProvider;
import com.microsoft.kiota.RequestInformation;

import java.util.concurrent.CompletableFuture;

public class OboAuthenticationProvider implements IAuthenticationProvider {

    private final OboTokenService oboTokenService;
    private final String userAccessToken;
    private final String senderEmail;

    public OboAuthenticationProvider(OboTokenService oboTokenService, String userAccessToken, String senderEmail) {
        this.oboTokenService = oboTokenService;
        this.userAccessToken = userAccessToken;
        this.senderEmail = senderEmail;
    }

    @Override
    public CompletableFuture<Void> authenticateRequest(RequestInformation requestInformation) {
        String graphToken = oboTokenService.getValidGraphToken(userAccessToken, senderEmail);
        requestInformation.addHeader("Authorization", "Bearer " + graphToken);
        return CompletableFuture.completedFuture(null);
    }
}