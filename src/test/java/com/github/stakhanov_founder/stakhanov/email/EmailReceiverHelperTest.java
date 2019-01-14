package com.github.stakhanov_founder.stakhanov.email;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.junit.Test;

import com.github.stakhanov_founder.stakhanov.email.model.EmailMessage;
import com.github.stakhanov_founder.stakhanov.email.model.SimpleEmailMessage;
import com.github.stakhanov_founder.stakhanov.model.MainControllerAction;
import com.github.stakhanov_founder.stakhanov.model.PostSlackMessageMainControllerAction;

import allbegray.slack.webapi.method.chats.ChatPostMessageMethod;

public class EmailReceiverHelperTest {

    @Test
    public void testProcessEmail_baseCase_addCorrectInstructionToQueue() throws AddressException {
        Queue<MainControllerAction> queue = new LinkedList<>();
        EmailReceiverHelper helper = new EmailReceiverHelper("bot@myrobot.com", queue::add);
        EmailMessage incomingEmail = new SimpleEmailMessage(
                new InternetAddress("mainuser@mainuserdomain.com"),
                Arrays.asList(new InternetAddress("bot+channel.mychannel.abcdef@myrobot.com")),
                "This is my subject",
                "This is my body\n\nOn blablabla other person wrote:\n\nhi",
                "This is my body",
                Optional.empty()
                );

        helper.processEmail(incomingEmail);

        assertEquals(
                Arrays.asList(
                        new PostSlackMessageMainControllerAction(new ChatPostMessageMethod("abcdef", "This is my body"))),
                queue);
    }

    @Test
    public void testProcessEmail_simpleReply_addCorrectThreadTimestampId() throws AddressException {
        Queue<MainControllerAction> queue = new LinkedList<>();
        EmailReceiverHelper helper = new EmailReceiverHelper("bot@myrobot.com", queue::add);
        EmailMessage incomingEmail = new SimpleEmailMessage(
                new InternetAddress("mainuser@mainuserdomain.com"),
                Arrays.asList(new InternetAddress("bot+channel.mychannel.abcdef@myrobot.com")),
                "This is my subject",
                "This is my body\n\nOn blablabla other person wrote:\n\nhi",
                "This is my body",
                Optional.of("<slack.message.defaultworkspace.abcdef.123456789123456@stakhanov.stakhanov_founder.github.com>")
                );

        helper.processEmail(incomingEmail);

        assertEquals(
                "123456789.123456",
                ((PostSlackMessageMainControllerAction)queue.poll()).messageToSend.getThread_ts());
    }

    @Test
    public void testProcessEmail_replyToReply_addCorrectThreadTimestampId() throws AddressException {
        Queue<MainControllerAction> queue = new LinkedList<>();
        EmailReceiverHelper helper = new EmailReceiverHelper("bot@myrobot.com", queue::add);
        EmailMessage incomingEmail = new SimpleEmailMessage(
                new InternetAddress("mainuser@mainuserdomain.com"),
                Arrays.asList(new InternetAddress("bot+channel.mychannel.abcdef@myrobot.com")),
                "This is my subject",
                "This is my body\n\nOn blablabla other person wrote:\n\nhi",
                "This is my body",
                Optional.of("<slack.message.defaultworkspace.abcdef.123456789123456.123456789400000"
                        + "@stakhanov.stakhanov_founder.github.com>")
                );

        helper.processEmail(incomingEmail);

        assertEquals(
                "123456789.123456",
                ((PostSlackMessageMainControllerAction)queue.poll()).messageToSend.getThread_ts());
    }
}
