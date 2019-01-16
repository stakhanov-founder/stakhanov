package com.github.stakhanov_founder.stakhanov;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Test;

import com.github.stakhanov_founder.stakhanov.model.SlackStandardEvent;

public class MainControllerHelperTest {

    @Test
    public void testProcessInboxes_slackMessageFromMainUser_skipIt() throws IOException {
        Queue<SlackStandardEvent> outboxToPersonalEmail = new LinkedList<>();
        String slackMessageRawPayload = "{"
                + "\"type\":\"event_callback\","
                + "\"event\":{"
                + "   \"type\":\"message\","
                + "   \"user\":\"abcdef\"}"
                + "}";
        MainControllerHelper helper = new MainControllerHelper(
                "abcdef",
                new LinkedList<>(Arrays.asList(slackMessageRawPayload)),
                new LinkedList<>(),
                outboxToPersonalEmail,
                new LinkedList<>());

        helper.processInboxes();

        assertEquals(Collections.emptyList(), outboxToPersonalEmail);
    }

    @Test
    public void testProcessInboxes_slackMessageFromMainUser_removeFromQueue() throws IOException {
        Queue<String> inboxFromTeamSlack = new LinkedList<>(Arrays.asList("{"
                + "\"type\":\"event_callback\","
                + "\"event\":{"
                + "   \"type\":\"message\","
                + "   \"user\":\"abcdef\"}"
                + "}"));
        MainControllerHelper helper = new MainControllerHelper(
                "abcdef",
                inboxFromTeamSlack,
                new LinkedList<>(),
                new LinkedList<>(),
                new LinkedList<>());

        helper.processInboxes();

        assertEquals(Collections.emptyList(), inboxFromTeamSlack);
    }
}
