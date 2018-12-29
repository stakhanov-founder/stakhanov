package com.github.stakhanov_founder.stakhanov.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.stakhanov_founder.stakhanov.slack.eventreceiver.SlackEventPayload;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SlackStandardEvent extends SlackEventPayload {

    @JsonProperty("event")
    public SlackEventData eventData;
}
