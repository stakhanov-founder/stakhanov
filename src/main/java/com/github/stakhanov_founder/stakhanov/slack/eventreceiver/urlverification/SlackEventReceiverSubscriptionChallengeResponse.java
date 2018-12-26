package com.github.stakhanov_founder.stakhanov.slack.eventreceiver.urlverification;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SlackEventReceiverSubscriptionChallengeResponse {

	@JsonProperty
	public final String challenge;

	public SlackEventReceiverSubscriptionChallengeResponse(String challenge) {
		this.challenge = challenge;
	}
}
