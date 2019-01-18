package com.github.stakhanov_founder.stakhanov.email;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.stakhanov_founder.stakhanov.email.model.EmailMessage;
import com.github.stakhanov_founder.stakhanov.email.model.EmailTag;
import com.github.stakhanov_founder.stakhanov.email.model.SlackChannelEmailTag;
import com.github.stakhanov_founder.stakhanov.model.MainControllerAction;
import com.github.stakhanov_founder.stakhanov.model.PostSlackMessageMainControllerAction;
import com.github.stakhanov_founder.stakhanov.slack.SlackMessageIdentifier;

import allbegray.slack.webapi.method.chats.ChatPostMessageMethod;

class EmailReceiverHelper {

    private static final Logger logger = LoggerFactory.getLogger(EmailReceiverHelper.class);

    private final String botEmailAddress;
    private final Consumer<MainControllerAction> mainControllerInbox;
    private final EmailHelper helper = new EmailHelper();

    EmailReceiverHelper(String botEmailAddress, Consumer<MainControllerAction> mainControllerInbox) {
        this.botEmailAddress = botEmailAddress;
        this.mainControllerInbox = mainControllerInbox;
    }

    void processEmail(EmailMessage email) {
        logger.debug("Email received:" + email);
        if (helper.isSameEmailAccount(email.getSender().getAddress(), botEmailAddress)) {
            return;
        }
        List<EmailTag> tags = EmailTags.extractAllTagsFromEmailRecipients(email, botEmailAddress);
        SlackChannelEmailTag channelTag = null;
        for (EmailTag tag : tags) {
            if (tag instanceof SlackChannelEmailTag && channelTag == null) {
                channelTag = (SlackChannelEmailTag)tag;
            }
        }
        if (channelTag != null && channelTag.channelId.isPresent()) {
            String slackMessageBody = extractBodyForSlackMessage(email);
            ChatPostMessageMethod messageToPost = new ChatPostMessageMethod(
                    channelTag.channelId.get(),
                    slackMessageBody);
            messageToPost.setAs_user(true);
            Optional<String> inReplyToHeader = email.getInReplyToHeader();
            if (inReplyToHeader.isPresent()) {
                Optional<SlackMessageIdentifier> lastSlackMessageInThread
                    = EmailMessageIds.extractMessageCoordinatesFromEmailMessageId(inReplyToHeader.get());
                if (lastSlackMessageInThread.isPresent()) {
                    double threadTimestampId = lastSlackMessageInThread
                            .get().threadTimestampId
                            .orElse(lastSlackMessageInThread.get().timestampId);
                    messageToPost.setThread_ts(String.format("%.6f", threadTimestampId));
                }
            }
            mainControllerInbox.accept(
                    new PostSlackMessageMainControllerAction(messageToPost));
        }
    }

    private String extractBodyForSlackMessage(EmailMessage email) {
        if (email == null) {
            return "";
        }
        if (email.getTextBodyWithoutQuotedText() != null) {
            return email.getTextBodyWithoutQuotedText();
        }
        if (email.getTextBody() != null) {
            return email.getTextBody();
        }
        return "";
    }
}
