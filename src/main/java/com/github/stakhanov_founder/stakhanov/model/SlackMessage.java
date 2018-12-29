package com.github.stakhanov_founder.stakhanov.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SlackMessage extends SlackEventData {

    @JsonProperty("subtype")
    public String subType;

    @JsonProperty
    public String text;

    @JsonProperty("user")
    public String senderId;

    @JsonProperty("channel")
    public String channelId;

    @JsonProperty("channel_type")
    public String channelType;
}
