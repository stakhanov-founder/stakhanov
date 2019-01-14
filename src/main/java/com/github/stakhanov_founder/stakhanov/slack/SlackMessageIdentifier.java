package com.github.stakhanov_founder.stakhanov.slack;

import java.util.Optional;

public class SlackMessageIdentifier {

    public final String channelId;
    public final double timestampId;
    public final Optional<Double> threadTimestampId;

    public SlackMessageIdentifier(String channelId, double timestampId, Optional<Double> threadTimestampId) {
        this.channelId = channelId;
        this.timestampId = timestampId;
        this.threadTimestampId = threadTimestampId;
    }
}
