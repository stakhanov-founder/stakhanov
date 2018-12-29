package com.github.stakhanov_founder.stakhanov;

import java.util.LinkedList;
import java.util.Queue;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stakhanov_founder.stakhanov.email.EmailSender;
import com.github.stakhanov_founder.stakhanov.model.SlackStandardEvent;
import com.github.stakhanov_founder.stakhanov.slack.dataproviders.CachedSlackUserDataProvider;
import com.github.stakhanov_founder.stakhanov.slack.eventreceiver.SlackEventPayload;
import com.github.stakhanov_founder.stakhanov.slack.eventreceiver.SlackEventReceiverApplication;

import allbegray.slack.SlackClientFactory;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
        String userEmailAddress = System.getenv("USER_EMAIL");
        String botEmailAddress = System.getenv("BOT_EMAIL");
        try {
            InternetAddress.parse(userEmailAddress, true);
            InternetAddress.parse(botEmailAddress, true);
        } catch (AddressException | NullPointerException ex) {
            logger.error("Wrong email address: " + userEmailAddress + " or " + botEmailAddress);
            System.exit(1);
        }
        String slackToken = System.getenv("SLACK_TOKEN");
        if (slackToken == null) {
            logger.error("Please set SLACK_TOKEN environment variable");
            System.exit(1);
        }

        Queue<String> inboxFromTeamSlack = new LinkedList<>();
        Queue<SlackStandardEvent> outboxToPersonalEmail = new LinkedList<>();

        new SlackEventReceiverApplication(inboxFromTeamSlack::add).run("server",
                "com/github/stakhanov_founder/stakhanov/slack/eventreceiver/configuration.yml");
        new EmailSender(
                outboxToPersonalEmail::poll,
                userEmailAddress,
                botEmailAddress,
                new CachedSlackUserDataProvider(SlackClientFactory.createWebApiClient(slackToken)))
            .start();

        while (true) {
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
            } else {
                Thread.sleep(500);
            }
        }
    }
}
