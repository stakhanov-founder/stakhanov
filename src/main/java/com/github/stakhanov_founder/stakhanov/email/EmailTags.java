package com.github.stakhanov_founder.stakhanov.email;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.stakhanov_founder.stakhanov.email.model.EmailMessage;
import com.github.stakhanov_founder.stakhanov.email.model.EmailTag;
import com.github.stakhanov_founder.stakhanov.email.model.SlackChannelEmailTag;
import com.github.stakhanov_founder.stakhanov.email.model.SlackUserEmailTag;

class EmailTags {

    private static final String SLACK_CHANNEL_TAG_TYPE = "channel";
    private static final String SLACK_USER_TAG_TYPE = "person";

    static String addTagToEmail(String emailAddress, EmailTag tag) {
        int indexOfArobasCharacter = emailAddress.indexOf('@');
        String userName = emailAddress.substring(0, indexOfArobasCharacter);
        String domain = emailAddress.substring(indexOfArobasCharacter + 1);
        String tagAsString = getTagAsString(tag);
        return userName + '+' + tagAsString + '@' + domain;
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
        return email.getToRecipients()
                .stream()
                .map(address -> extractEmailTag(address.getAddress(), botEmailAddress))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static Optional<EmailTag> extractEmailTag(String emailAddress, String emailAddressWithoutTag) {
        if (emailAddress == null) {
            return Optional.empty();
        }
        if (emailAddressWithoutTag == null) {
            throw new IllegalArgumentException("Null passed as email address without tag");
        }
        int indexOfArobas = emailAddressWithoutTag.indexOf('@');
        if (indexOfArobas < 0) {
            throw new IllegalArgumentException("Invalid email address without tag: " + emailAddressWithoutTag);
        }
        String username = emailAddressWithoutTag.substring(0, indexOfArobas);
        int indexOfArobasInActualEmailAddress = emailAddress.indexOf('@');
        if (indexOfArobasInActualEmailAddress < 0 || !emailAddress.startsWith(username)) {
            return Optional.empty();
        }
        return parseEmailTag(emailAddress.substring(username.length() + 1, indexOfArobasInActualEmailAddress));
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
