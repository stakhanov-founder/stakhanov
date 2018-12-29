package com.github.stakhanov_founder.stakhanov.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type",
    defaultImpl = SlackEventData.class)
@JsonSubTypes({@JsonSubTypes.Type(value = SlackMessage.class, name = "message")})
public class SlackEventData {

}
