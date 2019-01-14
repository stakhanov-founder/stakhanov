package com.github.stakhanov_founder.stakhanov.email;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.stakhanov_founder.stakhanov.model.SlackMessage;
import com.github.stakhanov_founder.stakhanov.slack.SlackMessageIdentifier;

class EmailMessageIds {

    private static final String DOMAIN = "stakhanov.stakhanov_founder.github.com";

    static String composeEmailMessageId(SlackMessage slackMessage) {
        if (slackMessage == null) {
            throw new IllegalArgumentException("Null passed as slack message");
        }
        return composeEmailMessageId(
                slackMessage.channelId,
                slackMessage.timestampId,
                slackMessage.threadTimestampId > 0 && slackMessage.threadTimestampId != slackMessage.timestampId
                    ? Optional.of(slackMessage.threadTimestampId) : Optional.empty());
    }

    static String composeEmailMessageId(String channelId, double timestampId) {
        return composeEmailMessageId(channelId, timestampId, Optional.empty());
    }

    static String composeEmailMessageId(String channelId, double timestampId, Optional<Double> threadTimestampId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
            .append("<")
            .append("slack.message.")
            .append("defaultworkspace.")
            .append(channelId + ".");
        if (threadTimestampId.isPresent()) {
            stringBuilder.append((long)(threadTimestampId.get() * 1_000_000) + ".");
        }
        stringBuilder
            .append((long)(timestampId * 1_000_000))
            .append("@")
            .append(DOMAIN)
            .append(">");
        return stringBuilder.toString();
    }

    static Optional<SlackMessageIdentifier> extractMessageCoordinatesFromEmailMessageId(String emailMessageId) {
        Pattern regex = Pattern.compile("<slack\\.message\\.defaultworkspace\\.(.+?)(?:\\.(\\d+?))?\\.(\\d+?)@" + DOMAIN + ">");
        Matcher matcher = regex.matcher(emailMessageId);
        if (matcher.matches()) {
            Optional<Double> threadTimestampId;
            if (matcher.group(2) != null) {
                threadTimestampId = Optional.of(Long.parseLong(matcher.group(2)) / 1_000_000.0);
            }
            else {
                threadTimestampId = Optional.empty();
            }
            return Optional.of(
                    new SlackMessageIdentifier(
                            matcher.group(1),
                            Long.parseLong(matcher.group(3)) / 1_000_000.0,
                            threadTimestampId));
        }
        return Optional.empty();
    }
}
