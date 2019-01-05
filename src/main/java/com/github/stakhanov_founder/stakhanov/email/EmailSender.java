package com.github.stakhanov_founder.stakhanov.email;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.function.Supplier;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;

import org.masukomi.aspirin.Aspirin;
import org.masukomi.aspirin.AspirinInternal;
import org.masukomi.aspirin.delivery.DeliveryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stakhanov_founder.stakhanov.dataproviders.SlackThreadMetadataSocket;
import com.github.stakhanov_founder.stakhanov.model.SlackStandardEvent;
import com.github.stakhanov_founder.stakhanov.slack.dataproviders.SlackMessageDataProvider;
import com.github.stakhanov_founder.stakhanov.slack.dataproviders.SlackUserDataProvider;

public class EmailSender extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);

    private final Supplier<SlackStandardEvent> emailQueue;
    private final DeliveryManager aspirinDeliveryManager;
    private final EmailSenderHelper helper;

    public EmailSender(Supplier<SlackStandardEvent> emailQueue, String userEmailAddress, String botEmailAddress,
            SlackUserDataProvider slackUserDataProvider, SlackMessageDataProvider slackMessageDataProvider,
            SlackThreadMetadataSocket slackThreadMetadataSocket)
            throws NoSuchFieldException, IllegalAccessException {
        this.emailQueue = emailQueue;

        Field deliveryManagerField = AspirinInternal.class.getDeclaredField("deliveryManager");
        deliveryManagerField.setAccessible(true);
        aspirinDeliveryManager = (DeliveryManager)deliveryManagerField.get(null);

        helper = new EmailSenderHelper(userEmailAddress, botEmailAddress, slackUserDataProvider, slackMessageDataProvider,
                slackThreadMetadataSocket);
    }

    @Override
    public void run() {
        while (true) {
            SlackStandardEvent slackEvent = emailQueue.get();
            try {
                if (slackEvent != null) {
                    MimeMessage message = AspirinInternal.createNewMimeMessage();
                    helper.setupMimeMessageToSend(message, slackEvent);
                    Aspirin.add(message);
                    synchronized (aspirinDeliveryManager) {
                        aspirinDeliveryManager.notifyAll();
                    }
                } else {
                    Thread.sleep(100);
                }
            } catch (AddressException | UnsupportedEncodingException ex) {
                logger.error("Wrong email address to send to : " + ex.getMessage());
            } catch (MessagingException ex) {
                try {
                    logger.error("Could not send email. Slack event : " + new ObjectMapper().writeValueAsString(slackEvent));
                } catch (JsonProcessingException e) {
                    logger.error("Could not send email. Slack event : " + slackEvent);
                }
            } catch (InterruptedException ex) {
                break;
            }
        }
    }
}
