package com.github.stakhanov_founder.stakhanov.email;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.mail.internet.InternetAddress;

import com.github.stakhanov_founder.stakhanov.email.model.EmailAddressComponents;
import com.github.stakhanov_founder.stakhanov.email.model.EmailMessage;
import com.github.stakhanov_founder.stakhanov.email.model.EmailTag;
import com.github.stakhanov_founder.stakhanov.email.model.SlackChannelEmailTag;
import com.github.stakhanov_founder.stakhanov.email.model.SlackUserEmailTag;

class EmailTags {

    private static final String SLACK_CHANNEL_TAG_TYPE = "channel";
    private static final String SLACK_USER_TAG_TYPE = "person";

    private static final EmailHelper helper = new EmailHelper();

    static String addTagToEmail(String emailAddress, EmailTag tag) {
        EmailAddressComponents emailAddressComponents
            = helper.decomposeEmailAddress(emailAddress);
        String tagAsString = getTagAsString(tag);
        return emailAddressComponents.userName + '+' + tagAsString
                + '@' + emailAddressComponents.domain;
    }

    private static String getTagAsString(EmailTag tag) {
        if (tag == null) {
            return null;
        }
        if (tag instanceof SlackChannelEmailTag) {
            SlackChannelEmailTag channelTag = (SlackChannelEmailTag)tag;
            String channelName = channelTag.channelName.orElse("");
            String channelId = channelTag.channelId.orElse("");
            return SLACK_CHANNEL_TAG_TYPE + '.' + channelName + '.' + channelId;
        }
        if (tag instanceof SlackUserEmailTag) {
            SlackUserEmailTag userTag = (SlackUserEmailTag)tag;
            String userName = userTag.userName.orElse("");
            String userId = userTag.userId.orElse("");
            return SLACK_USER_TAG_TYPE + '.' + userName + '.' + userId;
        }
        throw new IllegalStateException("Unknow email tag type: " + tag.getClass().getName());
    }

    static List<EmailTag> extractAllTagsFromEmailRecipients(EmailMessage email, String botEmailAddress) {
        EmailAddressComponents botEmailAddressComponents = helper.decomposeEmailAddress(botEmailAddress);
        return Stream.concat(email.getToRecipients().stream(), email.getCcRecipients().stream())
                .map(InternetAddress::getAddress)
                .map(helper::decomposeEmailAddress)
                .filter(emailAddressComponents -> helper.isSameEmailAccount(
                        emailAddressComponents, botEmailAddressComponents))
                .map(emailAddressComponents -> emailAddressComponents.tag)
                .map(EmailTags::parseEmailTag)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static Optional<EmailTag> parseEmailTag(String input) {
        int firstDot = input.indexOf('.');
        if (firstDot < 0) {
            return Optional.empty();
        }
        String type = input.substring(0, firstDot);
        String typeSpecificData = input.substring(firstDot + 1);
        switch (type) {
        case SLACK_USER_TAG_TYPE:
            return parseSlackUserEmailTag(typeSpecificData);
        case SLACK_CHANNEL_TAG_TYPE:
            return parseSlackChannelEmailTag(typeSpecificData);
        default:
            throw new IllegalStateException("Unknown email tag type: " + input);
        }
    }

    private static Optional<EmailTag> parseSlackUserEmailTag(String userSpecificData) {
        int lastDot = userSpecificData.lastIndexOf('.');
        if (lastDot < 0) {
            return Optional.empty();
        }
        return Optional.of(new SlackUserEmailTag(
                Optional.of(userSpecificData.substring(lastDot + 1)),
                Optional.of(userSpecificData.substring(0, lastDot))
                ));
    }

    private static Optional<EmailTag> parseSlackChannelEmailTag(String channelSpecificData) {
        int lastDot = channelSpecificData.lastIndexOf('.');
        if (lastDot < 0) {
            return Optional.empty();
        }
        return Optional.of(new SlackChannelEmailTag(
                Optional.of(channelSpecificData.substring(0, lastDot)),
                Optional.of(channelSpecificData.substring(lastDot + 1))
                ));
    }
}
