package com.github.stakhanov_founder.stakhanov.slack.eventreceiver.urlverification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.stakhanov_founder.stakhanov.slack.eventreceiver.SlackEventPayload;

public class SlackEventReceiverSubscriptionChallengePayload extends SlackEventPayload {

	@JsonProperty
	public String challenge;
}
