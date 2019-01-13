package com.github.stakhanov_founder.stakhanov.email;

import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.stakhanov_founder.stakhanov.email.model.EmailMessage;
import com.github.stakhanov_founder.stakhanov.email.model.EmailTag;
import com.github.stakhanov_founder.stakhanov.email.model.SlackChannelEmailTag;
import com.github.stakhanov_founder.stakhanov.model.MainControllerAction;
import com.github.stakhanov_founder.stakhanov.model.PostSlackMessageMainControllerAction;

import allbegray.slack.webapi.method.chats.ChatPostMessageMethod;

class EmailReceiverHelper {

    private static final Logger logger = LoggerFactory.getLogger(EmailReceiverHelper.class);

    private final String botEmailAddress;
    private final Consumer<MainControllerAction> mainControllerInbox;

    EmailReceiverHelper(String botEmailAddress, Consumer<MainControllerAction> mainControllerInbox) {
        this.botEmailAddress = botEmailAddress;
        this.mainControllerInbox = mainControllerInbox;
    }

    void processEmail(EmailMessage email) {
        logger.debug("Email received:" + email);
        List<EmailTag> tags = EmailTags.extractAllTagsFromEmailRecipients(email, botEmailAddress);
        SlackChannelEmailTag channelTag = null;
        for (EmailTag tag : tags) {
            if (tag instanceof SlackChannelEmailTag && channelTag == null) {
                channelTag = (SlackChannelEmailTag)tag;
            }
        }
        if (channelTag != null && channelTag.channelId.isPresent()) {
            ChatPostMessageMethod messageToPost = new ChatPostMessageMethod(
                    channelTag.channelId.get(),
                    email.getTextBody());
            messageToPost.setAs_user(true);
            mainControllerInbox.accept(
                    new PostSlackMessageMainControllerAction(messageToPost));
        }
    }
}
