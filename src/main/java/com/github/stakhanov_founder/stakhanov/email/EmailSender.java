package com.github.stakhanov_founder.stakhanov.email;

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

import com.github.stakhanov_founder.stakhanov.model.EmailToSend;

public class EmailSender extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);

    private final Supplier<EmailToSend> emailQueue;
    private final DeliveryManager aspirinDeliveryManager;
    private final EmailSenderHelper helper;

    public EmailSender(Supplier<EmailToSend> emailQueue, String domain) throws NoSuchFieldException, IllegalAccessException {
        this.emailQueue = emailQueue;

        Field deliveryManagerField = AspirinInternal.class.getDeclaredField("deliveryManager");
        deliveryManagerField.setAccessible(true);
        aspirinDeliveryManager = (DeliveryManager)deliveryManagerField.get(null);

        helper = new EmailSenderHelper(domain);
    }

    @Override
    public void run() {
        while (true) {
            EmailToSend emailToSend = emailQueue.get();
            try {
                if (emailToSend != null) {
                    MimeMessage message = AspirinInternal.createNewMimeMessage();
                    helper.setupMimeMessageToSend(message, emailToSend);
                    Aspirin.add(message);
                    synchronized (aspirinDeliveryManager) {
                        aspirinDeliveryManager.notifyAll();
                    }
                } else {
                    Thread.sleep(100);
                }
            } catch (AddressException ex) {
                logger.error("Wrong email address to send to : " + ex.getMessage());
            } catch (MessagingException ex) {
                logger.error("Could not send email. Email content : " + emailToSend);
            } catch (InterruptedException ex) {
                break;
            }
        }
    }
}
