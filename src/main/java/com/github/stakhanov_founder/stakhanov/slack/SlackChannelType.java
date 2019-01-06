package com.github.stakhanov_founder.stakhanov.slack;

public enum SlackChannelType {

    CHANNEL("channel"),
    GROUP("group"),
    DIRECT_MESSAGE("directMessage");

    public final String description;

    private SlackChannelType(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

    public static SlackChannelType fromSlackApiType(String slackApiType) {
        if (slackApiType == null) {
            return null;
        }
        switch (slackApiType) {
        case "channel": return CHANNEL;
        case "mpim": return GROUP;
        case "im": return DIRECT_MESSAGE;
        default: throw new IllegalStateException("Unknow channel type: " + slackApiType);
        }
    }
}
