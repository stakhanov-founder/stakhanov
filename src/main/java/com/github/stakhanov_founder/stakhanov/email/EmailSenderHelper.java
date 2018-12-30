package com.github.stakhanov_founder.stakhanov.email;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stakhanov_founder.stakhanov.model.SlackEventData;
import com.github.stakhanov_founder.stakhanov.model.SlackMessage;
import com.github.stakhanov_founder.stakhanov.model.SlackStandardEvent;
import com.github.stakhanov_founder.stakhanov.slack.MentionLocation;
import com.github.stakhanov_founder.stakhanov.slack.SlackHelper;
import com.github.stakhanov_founder.stakhanov.slack.dataproviders.SlackUserDataProvider;
import com.google.common.base.Strings;

import allbegray.slack.type.User;

class EmailSenderHelper {

    private final String userEmailAddress;
    private final String botEmailAddress;
    private final SlackUserDataProvider slackuserDataProvider;

    EmailSenderHelper(String userEmailAddress, String botEmailAddress, SlackUserDataProvider slackuserDataProvider) {
        this.userEmailAddress = userEmailAddress;
        this.botEmailAddress = botEmailAddress;
        this.slackuserDataProvider = slackuserDataProvider;
    }

    void setupMimeMessageToSend(MimeMessage emptyMimeMessage, SlackStandardEvent slackEvent)
            throws UnsupportedEncodingException, MessagingException {
        SlackEventData eventData = slackEvent.eventData;
        if (eventData instanceof SlackMessage) {
            SlackMessage message = (SlackMessage)eventData;
            User sender = slackuserDataProvider.getUser(message.senderId);
            String senderDisplayName = sender.getProfile().getReal_name();
            if (senderDisplayName == null) {
                if (!Strings.isNullOrEmpty(sender.getProfile().getFirst_name())) {
                    senderDisplayName = sender.getProfile().getFirst_name() + " " + sender.getProfile().getLast_name();
                }
                else {
                    senderDisplayName = sender.getName();
                }
            }
            List<MentionLocation> directMentions = new SlackHelper().extractDirectMentions(message.text);
            String textWithResolvedMentions = resolveDirectMentions(message.text, directMentions);
            if (message.subType == null) {
                emptyMimeMessage.setFrom(new InternetAddress(
                        addLabelsToEmailAddress(botEmailAddress, "person", sender.getName(), message.senderId),
                        senderDisplayName));
                emptyMimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(userEmailAddress));
                emptyMimeMessage.setSubject(generateEmailSubjectFromSlackMessage(textWithResolvedMentions));
                emptyMimeMessage.setText(textWithResolvedMentions);
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

    private String addLabelsToEmailAddress(String emailAddress, String... labels) {
        int indexOfArobasCharacter = emailAddress.indexOf('@');
        String userName = emailAddress.substring(0, indexOfArobasCharacter);
        String domain = emailAddress.substring(indexOfArobasCharacter + 1);
        return userName + '+' + String.join(".", labels) + '@' + domain;
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
        emptyMimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(userEmailAddress));
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
}
