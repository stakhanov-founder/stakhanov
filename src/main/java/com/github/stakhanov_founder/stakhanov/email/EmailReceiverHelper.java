package com.github.stakhanov_founder.stakhanov.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.stakhanov_founder.stakhanov.email.model.EmailMessage;

class EmailReceiverHelper {

    private static final Logger logger = LoggerFactory.getLogger(EmailReceiverHelper.class);

    void processEmail(EmailMessage email) {
        logger.debug("Email received:" + email);
    }
}
