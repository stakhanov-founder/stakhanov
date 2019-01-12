package com.github.stakhanov_founder.stakhanov.email;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.stakhanov_founder.stakhanov.email.model.EmailMessage;
import com.github.stakhanov_founder.stakhanov.model.MainControllerAction;

class EmailReceiverHelper {

    private static final Logger logger = LoggerFactory.getLogger(EmailReceiverHelper.class);

    private final Consumer<MainControllerAction> mainControllerInbox;

    EmailReceiverHelper(Consumer<MainControllerAction> mainControllerInbox) {
        this.mainControllerInbox = mainControllerInbox;
    }

    void processEmail(EmailMessage email) {
        logger.debug("Email received:" + email);
    }
}
