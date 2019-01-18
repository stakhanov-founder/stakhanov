package com.github.stakhanov_founder.stakhanov.email.microsoftsdk;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.stakhanov_founder.stakhanov.email.model.EmailMessage;
import com.microsoft.graph.models.extensions.Message;
import com.microsoft.graph.models.extensions.Recipient;

public class MicrosoftEmailMessage implements EmailMessage {

    private static final Logger logger = LoggerFactory.getLogger(MicrosoftEmailMessage.class);

    private final Message microsoftMessage;

    public MicrosoftEmailMessage(Message microsoftMessage) {
        if (microsoftMessage == null) {
            throw new IllegalArgumentException("Null passed as email message from Microsoft to "
                    + MicrosoftEmailMessage.class.getSimpleName() + "'s constructor");
        }
        this.microsoftMessage = microsoftMessage;
    }

    @Override
    public InternetAddress getSender() {
        return addressFromMicrosoftRecipient(microsoftMessage.from);
    }

    @Override
    public List<InternetAddress> getToRecipients() {
        return standardInternetAddressListFromMicrosoftList(microsoftMessage.toRecipients);
    }

    @Override
    public List<InternetAddress> getCcRecipients() {
        return standardInternetAddressListFromMicrosoftList(microsoftMessage.ccRecipients);
    }

    private List<InternetAddress> standardInternetAddressListFromMicrosoftList(
            List<Recipient> microsoftList) {
        if (microsoftList == null) {
            return Collections.emptyList();
        }
        return microsoftList
                .stream()
                .map(this::addressFromMicrosoftRecipient)
                .collect(Collectors.toList());
    }

    @Override
    public String getSubject() {
        return microsoftMessage.subject;
    }

    @Override
    public String getTextBody() {
        return microsoftMessage.body == null ? null : microsoftMessage.body.content;
    }

    private InternetAddress addressFromMicrosoftRecipient(Recipient recipient) {
        if (recipient == null || recipient.emailAddress == null) {
            return null;
        }
        try {
            return new InternetAddress(recipient.emailAddress.address, recipient.emailAddress.name);
        }
        catch (UnsupportedEncodingException ex) {
            logger.error("Could not interpret an email message member", ex);
            return null;
        }
    }

    @Override
    public String toString() {
        return "From: " + getSender() + ", subject: " + getSubject();
    }

    @Override
    public String getTextBodyWithoutQuotedText() {
        return microsoftMessage.uniqueBody.content;
    }

    @Override
    public Optional<String> getInReplyToHeader() {
        return microsoftMessage
                .internetMessageHeaders
                .stream()
                .filter(header -> "In-Reply-To".equals(header.name))
                .map(header -> header.value)
                .findFirst();
    }
}
