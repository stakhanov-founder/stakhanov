package com.github.stakhanov_founder.stakhanov.email.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.mail.internet.InternetAddress;

public class SimpleEmailMessage implements EmailMessage {

    private final InternetAddress sender;
    private final List<InternetAddress> toRecipients;
    private final String subject;
    private final String textBody;
    private final String textBodyWithoutQuotedText;
    private final Optional<String> inReplyToHeader;

    public SimpleEmailMessage(InternetAddress sender, List<InternetAddress> toRecipients, String subject, String textBody,
            String textBodyWithoutQuotedText, Optional<String> inReplyToHeader) {
        this.sender = sender;
        this.toRecipients = Collections.unmodifiableList(new ArrayList<>(toRecipients));
        this.subject = subject;
        this.textBody = textBody;
        this.textBodyWithoutQuotedText = textBodyWithoutQuotedText;
        this.inReplyToHeader = inReplyToHeader;
    }

    @Override
    public InternetAddress getSender() {
        return sender;
    }

    @Override
    public List<InternetAddress> getToRecipients() {
        return toRecipients;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public String getTextBody() {
        return textBody;
    }

    @Override
    public String getTextBodyWithoutQuotedText() {
        return textBodyWithoutQuotedText;
    }

    @Override
    public Optional<String> getInReplyToHeader() {
        return inReplyToHeader;
    }
}
