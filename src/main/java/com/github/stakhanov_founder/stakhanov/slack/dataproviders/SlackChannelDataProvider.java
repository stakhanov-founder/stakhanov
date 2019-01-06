package com.github.stakhanov_founder.stakhanov.slack.dataproviders;

import java.util.Optional;

import allbegray.slack.type.Channel;

public interface SlackChannelDataProvider {

    Optional<Channel> getChannelDetails(String channelId);
}
