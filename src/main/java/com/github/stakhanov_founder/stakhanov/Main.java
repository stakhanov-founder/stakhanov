package com.github.stakhanov_founder.stakhanov;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.LinkedList;
import java.util.Queue;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stakhanov_founder.stakhanov.dataproviders.SimpleSlackThreadMetadataSocket;
import com.github.stakhanov_founder.stakhanov.email.EmailSender;
import com.github.stakhanov_founder.stakhanov.model.SlackStandardEvent;
import com.github.stakhanov_founder.stakhanov.slack.dataproviders.SimpleSlackMessageDataProvider;
import com.github.stakhanov_founder.stakhanov.slack.dataproviders.CachedSlackChannelDataProvider;
import com.github.stakhanov_founder.stakhanov.slack.dataproviders.CachedSlackGroupDataProvider;
import com.github.stakhanov_founder.stakhanov.slack.dataproviders.CachedSlackUserDataProvider;
import com.github.stakhanov_founder.stakhanov.slack.eventreceiver.SlackEventPayload;
import com.github.stakhanov_founder.stakhanov.slack.eventreceiver.SlackEventReceiverApplication;

import allbegray.slack.SlackClientFactory;
import allbegray.slack.webapi.SlackWebApiClient;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
        String mainUserSlackId = System.getenv("USER_SLACK_ID");
        if (mainUserSlackId == null) {
            logger.error("Please set USER_SLACK_ID environment variable");
            System.exit(1);
        }
        String mainUserEmailAddress = System.getenv("USER_EMAIL");
        String botEmailAddress = System.getenv("BOT_EMAIL");
        try {
            InternetAddress.parse(mainUserEmailAddress, true);
            InternetAddress.parse(botEmailAddress, true);
        } catch (AddressException | NullPointerException ex) {
            logger.error("Wrong email address: " + mainUserEmailAddress + " or " + botEmailAddress);
            System.exit(1);
        }
        String slackToken = System.getenv("SLACK_TOKEN");
        if (slackToken == null) {
            logger.error("Please set SLACK_TOKEN environment variable");
            System.exit(1);
        }
        SlackWebApiClient slackWebApiClient = SlackClientFactory.createWebApiClient(slackToken);

        Class.forName("org.postgresql.Driver");
        Connection databaseConnection = DriverManager.getConnection(DatabaseConnectionString.getDatabaseJdbcConnectionString());

        Queue<String> inboxFromTeamSlack = new LinkedList<>();
        Queue<SlackStandardEvent> outboxToPersonalEmail = new LinkedList<>();

        new SlackEventReceiverApplication(inboxFromTeamSlack::add).run("server",
                "com/github/stakhanov_founder/stakhanov/slack/eventreceiver/configuration.yml");
        new EmailSender(
                outboxToPersonalEmail::poll,
                mainUserSlackId,
                mainUserEmailAddress,
                botEmailAddress,
                new CachedSlackUserDataProvider(slackWebApiClient),
                new SimpleSlackMessageDataProvider(slackWebApiClient),
                new CachedSlackChannelDataProvider(slackWebApiClient),
                new CachedSlackGroupDataProvider(slackWebApiClient),
                new SimpleSlackThreadMetadataSocket(databaseConnection))
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
