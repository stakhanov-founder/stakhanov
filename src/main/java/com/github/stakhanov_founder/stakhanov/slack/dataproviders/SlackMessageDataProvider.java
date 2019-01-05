package com.github.stakhanov_founder.stakhanov.slack.dataproviders;

import java.util.Optional;

import allbegray.slack.type.Message;

public interface SlackMessageDataProvider {

    Optional<Message> getMessage(String channelId, double messageTimestampId);
}
