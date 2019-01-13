package com.github.stakhanov_founder.stakhanov.slack;

import allbegray.slack.webapi.method.chats.ChatPostMessageMethod;

public class PostMessageSlackOperation implements SlackOperation {

    public final ChatPostMessageMethod messageToPost;

    public PostMessageSlackOperation(ChatPostMessageMethod messageToPost) {
        this.messageToPost = messageToPost;
    }
}
