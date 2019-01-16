package com.github.stakhanov_founder.stakhanov;

import java.io.IOException;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stakhanov_founder.stakhanov.model.MainControllerAction;
import com.github.stakhanov_founder.stakhanov.model.PostSlackMessageMainControllerAction;
import com.github.stakhanov_founder.stakhanov.model.SlackStandardEvent;
import com.github.stakhanov_founder.stakhanov.slack.PostMessageSlackOperation;
import com.github.stakhanov_founder.stakhanov.slack.SlackOperation;
import com.github.stakhanov_founder.stakhanov.slack.eventreceiver.SlackEventPayload;

class MainControllerHelper {

    private final static Logger logger = LoggerFactory.getLogger(MainControllerHelper.class);

    private final Queue<String> inboxFromTeamSlack;
    private final Queue<MainControllerAction> inboxFromPersonalEmail;
    private final Queue<SlackStandardEvent> outboxToPersonalEmail;
    private final Queue<SlackOperation> outboxToTeamSlack;

    MainControllerHelper(Queue<String> inboxFromTeamSlack, Queue<MainControllerAction> inboxFromPersonalEmail,
            Queue<SlackStandardEvent> outboxToPersonalEmail, Queue<SlackOperation> outboxToTeamSlack) {
        if (inboxFromTeamSlack == null || inboxFromPersonalEmail == null || outboxToPersonalEmail == null
                || outboxToTeamSlack == null) {
            throw new IllegalArgumentException("Null passed as parameter to " + MainControllerHelper.class.getSimpleName()
                    + "'s constructor");
        }
        this.inboxFromTeamSlack = inboxFromTeamSlack;
        this.inboxFromPersonalEmail = inboxFromPersonalEmail;
        this.outboxToPersonalEmail = outboxToPersonalEmail;
        this.outboxToTeamSlack = outboxToTeamSlack;
    }

    void processInboxes() throws IOException {
        if (!inboxFromTeamSlack.isEmpty()) {
            String rawEvent = inboxFromTeamSlack.remove();
            SlackEventPayload eventPayload = new ObjectMapper().readValue(
                    rawEvent,
                    SlackEventPayload.class);
            if (eventPayload instanceof SlackStandardEvent) {
                outboxToPersonalEmail.add((SlackStandardEvent)eventPayload);
            } else {
                logger.error("A non standard slack event reached the main loop: " + rawEvent);
            }
        }
        if (!inboxFromPersonalEmail.isEmpty()) {
            try {
                MainControllerAction actionToCarryOut = inboxFromPersonalEmail.peek();
                logger.debug("Instruction received for main controller to carry out: " + actionToCarryOut);
                if (actionToCarryOut instanceof PostSlackMessageMainControllerAction) {
                    PostSlackMessageMainControllerAction postSlackMessageAction
                        = (PostSlackMessageMainControllerAction) actionToCarryOut;
                    outboxToTeamSlack.add(new PostMessageSlackOperation(postSlackMessageAction.messageToSend));
                }
                inboxFromPersonalEmail.poll();
            }
            catch (Exception ex) {
                logger.error("Exception occurred while processing instruction from personal email", ex);
            }
        }
    }

    boolean allDone() {
        return inboxFromTeamSlack.isEmpty() && inboxFromPersonalEmail.isEmpty();
    }
}
