package com.github.stakhanov_founder.stakhanov.email;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stakhanov_founder.stakhanov.dataproviders.SlackThreadMetadataSocket;
import com.github.stakhanov_founder.stakhanov.email.model.SlackChannelEmailTag;
import com.github.stakhanov_founder.stakhanov.email.model.SlackUserEmailTag;
import com.github.stakhanov_founder.stakhanov.model.SlackEventData;
import com.github.stakhanov_founder.stakhanov.model.SlackMessage;
import com.github.stakhanov_founder.stakhanov.model.SlackMessageThreadStatus;
import com.github.stakhanov_founder.stakhanov.model.SlackStandardEvent;
import com.github.stakhanov_founder.stakhanov.slack.MentionLocation;
import com.github.stakhanov_founder.stakhanov.slack.SlackChannelType;
import com.github.stakhanov_founder.stakhanov.slack.SlackHelper;
import com.github.stakhanov_founder.stakhanov.slack.dataproviders.SlackChannelDataProvider;
import com.github.stakhanov_founder.stakhanov.slack.dataproviders.SlackGroupDataProvider;
import com.github.stakhanov_founder.stakhanov.slack.dataproviders.SlackMessageDataProvider;
import com.github.stakhanov_founder.stakhanov.slack.dataproviders.SlackUserDataProvider;
import com.google.common.base.Strings;

import allbegray.slack.type.Channel;
import allbegray.slack.type.Group;
import allbegray.slack.type.User;

class EmailSenderHelper {

    private static final Logger logger = LoggerFactory.getLogger(EmailSenderHelper.class);

    private final String mainUserSlackId;
    private final String mainUserEmailAddress;
    private final String botEmailAddress;
    private final SlackUserDataProvider slackuserDataProvider;
    private final SlackMessageDataProvider slackMessageDataProvider;
    private final SlackChannelDataProvider slackChannelDataProvider;
    private final SlackGroupDataProvider slackGroupDataProvider;
    private final SlackThreadMetadataSocket slackThreadMetadataSocket;
    private final SlackHelper slackHelper = new SlackHelper();

    EmailSenderHelper(String mainUserSlackId, String mainUserEmailAddress, String botEmailAddress,
            SlackUserDataProvider slackuserDataProvider, SlackMessageDataProvider slackMessageDataProvider,
            SlackChannelDataProvider slackChannelDataProvider, SlackGroupDataProvider slackGroupDataProvider,
            SlackThreadMetadataSocket slackThreadMetadataSocket) {
        this.mainUserSlackId = mainUserSlackId;
        this.mainUserEmailAddress = mainUserEmailAddress;
        this.botEmailAddress = botEmailAddress;
        this.slackuserDataProvider = slackuserDataProvider;
        this.slackMessageDataProvider = slackMessageDataProvider;
        this.slackChannelDataProvider = slackChannelDataProvider;
        this.slackGroupDataProvider = slackGroupDataProvider;
        this.slackThreadMetadataSocket = slackThreadMetadataSocket;
    }

    void setupMimeMessageToSend(MimeMessage emptyMimeMessage, SlackStandardEvent slackEvent)
            throws UnsupportedEncodingException, MessagingException {
        SlackEventData eventData = slackEvent.eventData;
        if (eventData instanceof SlackMessage) {
            SlackMessage message = (SlackMessage)eventData;
            List<MentionLocation> directMentions = slackHelper.extractDirectMentions(message.text);
            String textWithResolvedMentions = resolveDirectMentions(message.text, directMentions);
            final SlackMessageThreadStatus messageThreadStatus = getSlackMessageThreadStatus(message);
            if (message.subType == null) {
                emptyMimeMessage.setFrom(
                        getBotEmailForPerson(
                                slackuserDataProvider.getUser(message.senderId)));
                Message.RecipientType mainUserRecipientType =
                        SlackChannelType.fromSlackApiType(message.channelType) == SlackChannelType.CHANNEL
                            ? Message.RecipientType.BCC
                                : Message.RecipientType.TO;
                emptyMimeMessage.addRecipient(mainUserRecipientType, new InternetAddress(mainUserEmailAddress));
                String threadSubject = computeThreadSubjectForEmail(message, messageThreadStatus, textWithResolvedMentions);
                emptyMimeMessage.setSubject(computeSpecificMessageSubjectForEmail(threadSubject, messageThreadStatus));
                emptyMimeMessage.setText(textWithResolvedMentions);
                setMessageIdHeader(emptyMimeMessage, message);
                if (messageThreadStatus != SlackMessageThreadStatus.THREAD_START) {
                    emptyMimeMessage.setHeader("In-Reply-To",
                            EmailMessageIds.composeEmailMessageId(message.channelId, message.threadTimestampId));
                }
                addSlackMessageMetadataAsRecipients(message, emptyMimeMessage);
                saveThreadSubject(message, messageThreadStatus, threadSubject);
            } else {
                switch (message.subType) {
                default:
                    setupMimeMessageToSendForGenericSlackEvent(emptyMimeMessage, eventData);
                }
            }
        } else {
            setupMimeMessageToSendForGenericSlackEvent(emptyMimeMessage, eventData);
        }
    }

    private String generateEmailSubjectFromSlackMessage(String slackMessageText) {
        final int maxSubjectLength = 60;
        if (slackMessageText == null) {
            return "";
        }
        if (slackMessageText.length() <= maxSubjectLength) {
            return slackMessageText;
        }
        return slackMessageText.substring(0, maxSubjectLength) + "...";
    }

    private void setupMimeMessageToSendForGenericSlackEvent(MimeMessage emptyMimeMessage, SlackEventData eventData)
            throws UnsupportedEncodingException, MessagingException {
        emptyMimeMessage.setFrom(new InternetAddress(botEmailAddress, "Stakhanov"));
        emptyMimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(mainUserEmailAddress));
        emptyMimeMessage.setSubject("Message from Slack");
        try {
            emptyMimeMessage.setText(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(eventData));
        } catch (JsonProcessingException e) {
            emptyMimeMessage.setText("There was a problem displaying its content");
        }
    }

    private String resolveDirectMentions(String slackMessageText, List<MentionLocation> directMentions) {
        if (directMentions == null || directMentions.isEmpty()) {
            return slackMessageText;
        }
        Set<String> firstNamesAppearingTwice = findFirstNamesAppearingTwice(directMentions);
        StringBuilder stringBuilder = new StringBuilder();
        int endOfLastMention = 0;
        for (MentionLocation mentionLocation : directMentions) {
            stringBuilder.append(slackMessageText.substring(endOfLastMention, mentionLocation.start));
            User user = slackuserDataProvider.getUser(mentionLocation.id);
            String nameToDisplay = chooseNameToDisplay(user, firstNamesAppearingTwice);
            stringBuilder.append(nameToDisplay);
            endOfLastMention = mentionLocation.start + mentionLocation.length;
        }
        if (endOfLastMention < slackMessageText.length()) {
            stringBuilder.append(slackMessageText.substring(endOfLastMention));
        }
        return stringBuilder.toString();
    }

    private Set<String> findFirstNamesAppearingTwice(List<MentionLocation> directMentions) {
        if (directMentions == null) {
            return Collections.emptySet();
        }
        List<String> usersFirstNames = directMentions
                .stream()
                .filter(mentionLocation -> mentionLocation != null && !Strings.isNullOrEmpty(mentionLocation.id))
                .map(mentionLocation -> slackuserDataProvider.getUser(mentionLocation.id))
                .filter(user -> !Strings.isNullOrEmpty(user.getProfile().getFirst_name()))
                .collect(Collectors.toSet())
                .stream()
                .map(user -> user.getProfile().getFirst_name())
                .collect(Collectors.toList());

        Set<String> firstNamesAlreadySeen = new HashSet<>();
        Set<String> duplicates = new HashSet<>();
        for(String firstName : usersFirstNames) {
            if(!firstNamesAlreadySeen.add(firstName)) {
                duplicates.add(firstName);
            }
        }
        return duplicates;
    }

    private String chooseNameToDisplay(User user, Set<String> firstNamesAppearingTwice) {
        String firstName = user.getProfile().getFirst_name();
        if (!Strings.isNullOrEmpty(firstName) && !firstNamesAppearingTwice.contains(firstName)) {
            return firstName;
        }
        if (!Strings.isNullOrEmpty(user.getProfile().getReal_name())) {
            return user.getProfile().getReal_name();
        }
        return user.getName();
    }

    private SlackMessageThreadStatus getSlackMessageThreadStatus(SlackMessage message) {
        if (!slackHelper.isReply(message)) {
            return SlackMessageThreadStatus.THREAD_START;
        }
        if (slackThreadMetadataSocket.getThreadSubjectForEmail(message.channelId, message.threadTimestampId)
                .isPresent()) {
            return SlackMessageThreadStatus.REPLY;
        }
        return SlackMessageThreadStatus.REPLY_WITHOUT_THREAD_METADATA;
    }

    private Address getBotEmailForPerson(User person) throws UnsupportedEncodingException {
        String displayName = person.getProfile().getReal_name();
        if (displayName == null) {
            if (!Strings.isNullOrEmpty(person.getProfile().getFirst_name())) {
                displayName = person.getProfile().getFirst_name() + " " + person.getProfile().getLast_name();
            }
            else {
                displayName = person.getName();
            }
        }
        return new InternetAddress(
                EmailTags.addTagToEmail(
                        botEmailAddress,
                        new SlackUserEmailTag(Optional.of(person.getName()), Optional.of(person.getId()))),
                displayName);
    }

    private String computeThreadSubjectForEmail(SlackMessage message, SlackMessageThreadStatus threadStatus,
            String textWithResolvedMentions) {
        switch (threadStatus) {
        case THREAD_START:
            return generateEmailSubjectFromSlackMessage(textWithResolvedMentions);
        case REPLY:
            return slackThreadMetadataSocket.getThreadSubjectForEmail(message.channelId, message.threadTimestampId).get();
        case REPLY_WITHOUT_THREAD_METADATA:
            Optional<allbegray.slack.type.Message> threadStarter =
                slackMessageDataProvider.getMessage(message.channelId, message.threadTimestampId);
            if (threadStarter.isPresent()) {
                String threadStarterText = threadStarter.get().getText();
                List<MentionLocation> threadStarterDirectMentions = slackHelper.extractDirectMentions(threadStarterText);
                String threadStarterTextWithResolvedMentions = resolveDirectMentions(
                        threadStarterText, threadStarterDirectMentions);
                return generateEmailSubjectFromSlackMessage(threadStarterTextWithResolvedMentions);
            }
            logger.error("Could not get message that started thread for current message: "
                    + message.channelId + ", " + message.threadTimestampId);
            return "";
        default:
            throw new IllegalStateException();
        }
    }

    private String computeSpecificMessageSubjectForEmail(
            String threadSubjectForEmail, SlackMessageThreadStatus messageThreadStatus) {
        if (messageThreadStatus == null) {
            throw new IllegalArgumentException("Null passed as Slack message thread status");
        }
        switch (messageThreadStatus) {
        case THREAD_START:
            return threadSubjectForEmail;
        default:
            return "Re: " + threadSubjectForEmail;
        }
    }

    private void saveThreadSubject(SlackMessage slackMessage, SlackMessageThreadStatus messageThreadStatus, String threadSubject) {
        try {
            switch (messageThreadStatus) {
            case THREAD_START:
                slackThreadMetadataSocket.setThreadSubjectForEmail(slackMessage.channelId, slackMessage.timestampId, threadSubject);
                break;
            case REPLY_WITHOUT_THREAD_METADATA:
                slackThreadMetadataSocket.setThreadSubjectForEmail(slackMessage.channelId, slackMessage.threadTimestampId, threadSubject);
                break;
            default:
            }
        }
        catch (IOException ex) {
            logger.error("An error occurred while trying to save a thread's subject", ex);
        }
    }

    private void setMessageIdHeader(MimeMessage mimeMessage, SlackMessage slackMessage) throws MessagingException {
        mimeMessage.saveChanges();
        mimeMessage.setHeader("Message-ID", EmailMessageIds.composeEmailMessageId(slackMessage));
    }

    private void addSlackMessageMetadataAsRecipients(SlackMessage slackMessage, MimeMessage mimeMessage)
            throws MessagingException, UnsupportedEncodingException {
        SlackChannelType channelType = SlackChannelType.fromSlackApiType(slackMessage.channelType);
        switch (channelType) {
        case CHANNEL:
            Optional<Channel> channelDetails = slackChannelDataProvider.getChannelDetails(slackMessage.channelId);
            String channelName;
            if (channelDetails.isPresent()) {
                channelName = channelDetails.get().getName();
            }
            else {
                channelName = "unknownChannel";
                logger.error("Could not get details of channel " + slackMessage.channelId);
            }
            mimeMessage.addRecipient(
                    Message.RecipientType.TO,
                    new InternetAddress(
                            EmailTags.addTagToEmail(
                                    botEmailAddress,
                                    new SlackChannelEmailTag(
                                            Optional.of(channelName), Optional.of(slackMessage.channelId))),
                            channelDetails.isPresent() ? "#" + channelName : "Channel " + slackMessage.channelId));
            break;
        case GROUP:
            Optional<Group> groupDetails = slackGroupDataProvider.getGroupDetails(slackMessage.channelId);
            if (!groupDetails.isPresent()) {
                logger.error("Could not get details of group " + slackMessage.channelId);
            } else {
                for (String userId : groupDetails.get().getMembers()) {
                    if (!slackMessage.senderId.equals(userId)
                            && !mainUserSlackId.equals(userId)) {
                        mimeMessage.addRecipient(
                                Message.RecipientType.TO,
                                getBotEmailForPerson(slackuserDataProvider.getUser(userId)));
                    }
                }
            }
            break;
        default:
        }
    }
}
