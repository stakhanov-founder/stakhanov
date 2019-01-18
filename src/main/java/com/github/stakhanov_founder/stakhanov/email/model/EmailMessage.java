package com.github.stakhanov_founder.stakhanov.email.model;

import java.util.List;
import java.util.Optional;

import javax.mail.internet.InternetAddress;

public interface EmailMessage {

    InternetAddress getSender();

    List<InternetAddress> getToRecipients();

    List<InternetAddress> getCcRecipients();

    String getSubject();

    String getTextBody();

    String getTextBodyWithoutQuotedText();

    Optional<String> getInReplyToHeader();
}
