package com.github.stakhanov_founder.stakhanov.slack.eventreceiver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.stakhanov_founder.stakhanov.model.SlackStandardEvent;
import com.github.stakhanov_founder.stakhanov.slack.eventreceiver.urlverification.SlackEventReceiverSubscriptionChallengePayload;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type",
	defaultImpl = SlackEventPayload.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = SlackEventReceiverSubscriptionChallengePayload.class, name = "url_verification"),
    @JsonSubTypes.Type(value = SlackStandardEvent.class, name = "event_callback")})
public class SlackEventPayload {

	@JsonProperty
	String token;
}
