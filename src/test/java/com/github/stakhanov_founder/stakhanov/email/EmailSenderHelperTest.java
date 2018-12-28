package com.github.stakhanov_founder.stakhanov.email;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.Test;

import com.github.stakhanov_founder.stakhanov.model.EmailToSend;

public class EmailSenderHelperTest {

    @Test
    public void testEmailSenderHelper_baseCase_returnCorrectValue() throws MessagingException, IOException {
        EmailSenderHelper helper = new EmailSenderHelper("yeepee.com");
        EmailToSend input = new EmailToSend(
                "recipient@gmail.com", "replyto@gmail.com", "this is my subject", "this is my body");
        MimeMessage emptyMimeMessage = new MimeMessage((Session)null);

        helper.setupMimeMessageToSend(emptyMimeMessage, input);

        assertArrayEquals(
                new Object[] {
                    emptyMimeMessage.getAllRecipients(),
                    emptyMimeMessage.getReplyTo(),
                    emptyMimeMessage.getFrom(),
                    emptyMimeMessage.getSubject(),
                    emptyMimeMessage.getContent()
                },
                new Object[] {
                    new Address[] {new InternetAddress("recipient@gmail.com")},
                    new Address[] {new InternetAddress("replyto@gmail.com")},
                    new Address[] {new InternetAddress("messageFromSlack@yeepee.com")},
                    "this is my subject",
                    "this is my body"
                });
    }
}
