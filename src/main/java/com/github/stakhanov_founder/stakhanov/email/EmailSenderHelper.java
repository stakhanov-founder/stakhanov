package com.github.stakhanov_founder.stakhanov.email;

import java.io.UnsupportedEncodingException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stakhanov_founder.stakhanov.model.SlackEventData;
import com.github.stakhanov_founder.stakhanov.model.SlackMessage;
import com.github.stakhanov_founder.stakhanov.model.SlackStandardEvent;
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
            if (message.subType == null) {
                emptyMimeMessage.setFrom(new InternetAddress(
                        addLabelsToEmailAddress(botEmailAddress, "person", sender.getName(), message.senderId),
                        senderDisplayName));
                emptyMimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(userEmailAddress));
                emptyMimeMessage.setSubject(generateEmailSubjectFromSlackMessage(message.text));
                emptyMimeMessage.setText(message.text);
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
}
