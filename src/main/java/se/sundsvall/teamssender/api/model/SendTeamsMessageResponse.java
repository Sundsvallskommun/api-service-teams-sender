package se.sundsvall.teamssender.api.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendTeamsMessageResponse {

	private String message;

	public SendTeamsMessageResponse() {}

	public SendTeamsMessageResponse(String message) {
		this.message = message;
	}
}
