package com.github.stakhanov_founder.stakhanov.slack.eventreceiver;

import java.io.IOException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stakhanov_founder.stakhanov.slack.eventreceiver.urlverification.SlackEventReceiverSubscriptionChallengePayload;
import com.github.stakhanov_founder.stakhanov.slack.eventreceiver.urlverification.SlackEventReceiverSubscriptionChallengeResponse;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class SlackEventReceiverResource {

	@POST
	public Object handlePost(String rawPayload) throws JsonParseException, JsonMappingException, IOException {
		SlackEventPayload payload = new ObjectMapper().readValue(rawPayload, SlackEventPayload.class);
		if (payload instanceof SlackEventReceiverSubscriptionChallengePayload) {
			return handleUrlVerification((SlackEventReceiverSubscriptionChallengePayload)payload);
		}
		else {
			return true;
		}
	}

	private SlackEventReceiverSubscriptionChallengeResponse handleUrlVerification(
			SlackEventReceiverSubscriptionChallengePayload payload) {
		return new SlackEventReceiverSubscriptionChallengeResponse(payload.challenge);
	}
}
