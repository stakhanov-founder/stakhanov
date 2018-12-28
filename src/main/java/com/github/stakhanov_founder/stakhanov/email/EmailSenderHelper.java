package com.github.stakhanov_founder.stakhanov.email;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.github.stakhanov_founder.stakhanov.model.EmailToSend;

class EmailSenderHelper {

    private final String domain;

    EmailSenderHelper(String domain) {
        this.domain = domain;
    }

    void setupMimeMessageToSend(MimeMessage emptyMimeMessage, EmailToSend basicEmailData)
            throws AddressException, MessagingException {
        emptyMimeMessage.setFrom(new InternetAddress("messageFromSlack@" + domain));
        emptyMimeMessage.setReplyTo(new Address[] { new InternetAddress(basicEmailData.replyTo) });
        emptyMimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(basicEmailData.recipient));
        emptyMimeMessage.setSubject(basicEmailData.subject);
        emptyMimeMessage.setText(basicEmailData.body);
    }
}
