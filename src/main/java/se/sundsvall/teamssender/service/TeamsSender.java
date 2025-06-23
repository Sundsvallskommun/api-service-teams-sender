package se.sundsvall.teamssender.service;

import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;

public interface TeamsSender {

	void sendTeamsMessage(SendTeamsMessageRequest request);

	String getMunicipalityId();

	void setMunicipalityId(String municipalityId);

}
