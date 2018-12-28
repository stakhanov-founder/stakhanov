package com.github.stakhanov_founder.stakhanov.model;

public class EmailToSend {

    public final String recipient;
    public final String replyTo;
    public final String subject;
    public final String body;

    public EmailToSend(String recipient, String replyTo, String subject, String body) {
        this.recipient = recipient;
        this.replyTo = replyTo;
        this.subject = subject;
        this.body = body;
    }
}
