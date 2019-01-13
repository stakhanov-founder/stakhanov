package com.github.stakhanov_founder.stakhanov.email.model;

import java.util.Optional;

public class SlackChannelEmailTag implements EmailTag {

    public final Optional<String> channelName;
    public final Optional<String> channelId;

    public SlackChannelEmailTag(Optional<String> channelName, Optional<String> channelId) {
        this.channelName = channelName;
        this.channelId = channelId;
    }
}
