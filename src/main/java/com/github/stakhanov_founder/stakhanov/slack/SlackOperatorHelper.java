package com.github.stakhanov_founder.stakhanov.slack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import allbegray.slack.webapi.SlackWebApiClient;

class SlackOperatorHelper {

    private static final Logger logger = LoggerFactory.getLogger(SlackOperatorHelper.class);

    private final SlackWebApiClient slackClient;

    SlackOperatorHelper(SlackWebApiClient slackClient) {
        if (slackClient == null) {
            throw new IllegalArgumentException("Null passed as argument to "
                    + SlackOperatorHelper.class.getSimpleName() + "'s constructor");
        }
        this.slackClient = slackClient;
    }

    void carryOutOperation(SlackOperation operation) {
        if (operation == null) {
            logger.error("Null operation encountered in " + SlackOperatorHelper.class.getSimpleName());
            return;
        }
        if (operation instanceof PostMessageSlackOperation) {
            PostMessageSlackOperation postOperation = (PostMessageSlackOperation)operation;
            slackClient.postMessage(postOperation.messageToPost);
            return;
        }
    }
}
