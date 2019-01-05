package com.github.stakhanov_founder.stakhanov.slack.dataproviders;

import java.util.List;
import java.util.Optional;

import allbegray.slack.type.Message;
import allbegray.slack.webapi.SlackWebApiClient;

public class SimpleSlackMessageDataProvider implements SlackMessageDataProvider {

    private final SlackWebApiClient slackWebApiClient;

    public SimpleSlackMessageDataProvider(SlackWebApiClient slackWebApiClient) {
        this.slackWebApiClient = slackWebApiClient;
    }

    /**
     * For now, this method only supports messages posted in a channel. Groups and
     * direct messages are not yet supported.
     */
    @Override
    public Optional<Message> getMessage(String channelId, double messageTimestampId) {
        String messageTimestampIdString = String.format("%.6f", messageTimestampId);
        List<Message> messages = slackWebApiClient.getChannelHistory(
                channelId, messageTimestampIdString, messageTimestampIdString, true, 1, false).getMessages();
        if (messages != null && messages.size() > 0) {
            return Optional.of(messages.get(0));
        }
        return Optional.empty();
    }
}
