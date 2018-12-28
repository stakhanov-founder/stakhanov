package com.github.stakhanov_founder.stakhanov;

import java.util.LinkedList;
import java.util.Queue;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.stakhanov_founder.stakhanov.email.EmailSender;
import com.github.stakhanov_founder.stakhanov.model.EmailToSend;
import com.github.stakhanov_founder.stakhanov.slack.eventreceiver.SlackEventReceiverApplication;

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
        String domain = System.getenv("DOMAIN");
        if (domain == null) {
            domain = "stackanov-production.herokuapp.com";
        }

        Queue<String> inboxFromTeamSlack = new LinkedList<>();
        Queue<EmailToSend> outboxToPersonalEmail = new LinkedList<>();

        new SlackEventReceiverApplication(inboxFromTeamSlack::add).run("server",
                "com/github/stakhanov_founder/stakhanov/slack/eventreceiver/configuration.yml");
        new EmailSender(outboxToPersonalEmail::poll, domain)
            .start();

        while (true) {
            if (!inboxFromTeamSlack.isEmpty()) {
                outboxToPersonalEmail.add(
                        new EmailToSend(
                                userEmailAddress,
                                botEmailAddress,
                                "New message received from Slack",
                                inboxFromTeamSlack.remove()));
            } else {
                Thread.sleep(500);
            }
        }
    }
}
