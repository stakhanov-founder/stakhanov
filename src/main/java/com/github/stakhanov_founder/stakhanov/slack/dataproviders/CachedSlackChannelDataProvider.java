package com.github.stakhanov_founder.stakhanov.slack.dataproviders;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import allbegray.slack.type.Channel;
import allbegray.slack.webapi.SlackWebApiClient;

public class CachedSlackChannelDataProvider implements SlackChannelDataProvider {

    private final SlackWebApiClient slackClient;
    private Instant lastFetchOfData;
    private final Map<String, Channel> channelsById = new HashMap<>();

    public CachedSlackChannelDataProvider(SlackWebApiClient slackClient) {
        if (slackClient == null) {
            throw new IllegalArgumentException("Null passed as Slack client to "
                    + CachedSlackChannelDataProvider.class.getSimpleName() + "'s constructor");
        }
        this.slackClient = slackClient;
    }

    @Override
    public Optional<Channel> getChannelDetails(String id) {
        Channel channel = channelsById.get(id);
        if (channel == null || lastFetchOfData.plus(1, ChronoUnit.DAYS).isBefore(Instant.now())) {
            refreshData();
            channel = channelsById.get(id);
            if (channel == null) {
                return Optional.empty();
            }
        }
        return Optional.of(channel);
    }

    private void refreshData() {
        channelsById.clear();
        lastFetchOfData = Instant.now();
        List<Channel> channels = slackClient.getChannelList();
        for (Channel channel : channels) {
            channelsById.put(channel.getId(), channel);
        }
    }
}
