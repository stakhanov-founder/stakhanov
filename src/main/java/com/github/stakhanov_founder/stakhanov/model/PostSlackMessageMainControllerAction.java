package com.github.stakhanov_founder.stakhanov.model;

import com.google.common.base.Objects;

import allbegray.slack.webapi.method.chats.ChatPostMessageMethod;

public class PostSlackMessageMainControllerAction implements MainControllerAction {

    public final ChatPostMessageMethod messageToSend;

    public PostSlackMessageMainControllerAction(ChatPostMessageMethod messageToSend) {
        this.messageToSend = messageToSend;
    }

    @Override
    public boolean equals(Object otherObject) {
        if (otherObject == null || !(otherObject instanceof PostSlackMessageMainControllerAction)) {
            return false;
        }
        PostSlackMessageMainControllerAction other = (PostSlackMessageMainControllerAction)otherObject;
        return Objects.equal(this.messageToSend.getChannel(), other.messageToSend.getChannel())
                && Objects.equal(this.messageToSend.getText(), other.messageToSend.getText());
    }

    @Override
    public String toString() {
        if (messageToSend == null) {
            return "Null message to send";
        }
        return "Post to '" + messageToSend.getChannel() + "' text '" + messageToSend.getText() + "'";
    }
}
