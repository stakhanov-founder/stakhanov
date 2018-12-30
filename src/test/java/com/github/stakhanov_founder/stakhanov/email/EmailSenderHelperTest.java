package com.github.stakhanov_founder.stakhanov.email;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stakhanov_founder.stakhanov.model.SlackStandardEvent;
import com.github.stakhanov_founder.stakhanov.slack.dataproviders.SlackUserDataProvider;
import com.github.stakhanov_founder.stakhanov.slack.eventreceiver.SlackEventPayload;

import allbegray.slack.type.Profile;
import allbegray.slack.type.User;

public class EmailSenderHelperTest {

    private SlackUserDataProvider slackUserDataProvider = userId -> {
        User user = new User();
        Profile profile = new Profile();
        user.setProfile(profile);
        switch (userId) {
        case "abcdef":
            user.setId("abcdef");
            user.setName("john.doe");
            profile.setFirst_name("John");
            profile.setLast_name("Doe");
            profile.setReal_name("John W. Doe");
            break;
        case "mnopqr":
            user.setId("mnopqr");
            user.setName("lionel.logue");
            profile.setFirst_name("Lionel");
            profile.setLast_name("Logue");
            profile.setReal_name("Dr Lionel Logue");
            break;
        case "stuvw":
            user.setId("stuvw");
            user.setName("lionel.bouchet");
            profile.setFirst_name("Lionel");
            profile.setLast_name("Bouchet");
            profile.setReal_name("Lionel Bouchet");
            break;
        case "userWithoutFirstName":
            user.setId("userWithoutFirstName");
            user.setName("joe.dalton");
            profile.setLast_name("Dalton");
            profile.setReal_name("Joe Dalton");
            break;
        case "userWithEmptyProfile":
            user.setId("userWithEmptyProfile");
            user.setName("jack.dalton");
            break;
        }
        return user;
    };

    @Test
    public void testEmailSenderHelper_simpleMessage_returnCorrectValue()
            throws JsonParseException, JsonMappingException, IOException, MessagingException {
        EmailSenderHelper helper = new EmailSenderHelper("recipient@user.com", "bot@stakhanov.com", slackUserDataProvider);
        SlackStandardEvent input = (SlackStandardEvent)new ObjectMapper().readValue(
                "{"
                + "\"token\":\"mytoken\","
                + "\"event\": {"
                + "   \"type\":\"message\","
                + "   \"text\":\"this is my text\","
                + "   \"user\":\"abcdef\","
                + "   \"channel\":\"ghijkl\","
                + "   \"channel_type\":\"im\""
                + "},"
                + "\"type\":\"event_callback\""
                + "}",
                SlackEventPayload.class);
        MimeMessage emptyMimeMessage = new MimeMessage((Session)null);

        helper.setupMimeMessageToSend(emptyMimeMessage, input);

        assertArrayEquals(
                new Object[] {
                    emptyMimeMessage.getAllRecipients(),
                    emptyMimeMessage.getFrom(),
                    emptyMimeMessage.getSubject(),
                    emptyMimeMessage.getContent()
                },
                new Object[] {
                    new Address[] {new InternetAddress("recipient@user.com")},
                    new Address[] {new InternetAddress("bot+person.john.doe.abcdef@stakhanov.com", "John W. Doe")},
                    "this is my text",
                    "this is my text"
                });
    }

    @Test
    public void testEmailSenderHelper_longMessage_returnCorrectValue()
            throws JsonParseException, JsonMappingException, IOException, MessagingException {
        EmailSenderHelper helper = new EmailSenderHelper("recipient@user.com", "bot@stakhanov.com", slackUserDataProvider);
        SlackStandardEvent input = (SlackStandardEvent)new ObjectMapper().readValue(
                "{"
                + "\"token\":\"mytoken\","
                + "\"event\": {"
                + "   \"type\":\"message\","
                + "   \"text\":\"this is my long text. I don't know exactly what to say, I wish it could be more interesting\","
                + "   \"user\":\"abcdef\","
                + "   \"channel\":\"ghijkl\","
                + "   \"channel_type\":\"im\""
                + "},"
                + "\"type\":\"event_callback\""
                + "}",
                SlackEventPayload.class);
        MimeMessage emptyMimeMessage = new MimeMessage((Session)null);

        helper.setupMimeMessageToSend(emptyMimeMessage, input);

        assertArrayEquals(
                new Object[] {
                    emptyMimeMessage.getAllRecipients(),
                    emptyMimeMessage.getFrom(),
                    emptyMimeMessage.getSubject(),
                    emptyMimeMessage.getContent()
                },
                new Object[] {
                    new Address[] {new InternetAddress("recipient@user.com")},
                    new Address[] {new InternetAddress("bot+person.john.doe.abcdef@stakhanov.com", "John W. Doe")},
                    "this is my long text. I don't know exactly what to say, I wi...",
                    "this is my long text. I don't know exactly what to say, I wish it could be more interesting"
                });
    }

    @Test
    public void testEmailSenderHelper_unsupportedEventType_returnCorrectValue()
            throws JsonParseException, JsonMappingException, IOException, MessagingException {
        EmailSenderHelper helper = new EmailSenderHelper("recipient@user.com", "bot@stakhanov.com", slackUserDataProvider);
        SlackStandardEvent input = (SlackStandardEvent)new ObjectMapper().readValue(
                "{"
                + "\"token\":\"mytoken\","
                + "\"event\": {"
                + "   \"type\":\"reaction_added\","
                + "   \"user\":\"abcdef\","
                + "   \"reaction\":\"watermelon\""
                + "},"
                + "\"type\":\"event_callback\""
                + "}",
                SlackEventPayload.class);
        MimeMessage emptyMimeMessage = new MimeMessage((Session)null);

        helper.setupMimeMessageToSend(emptyMimeMessage, input);

        assertArrayEquals(
                new Object[] {
                    emptyMimeMessage.getAllRecipients(),
                    emptyMimeMessage.getFrom(),
                    emptyMimeMessage.getSubject(),
                    emptyMimeMessage.getContent()
                },
                new Object[] {
                    new Address[] {new InternetAddress("recipient@user.com")},
                    new Address[] {new InternetAddress("bot@stakhanov.com", "Stakhanov")},
                    "Message from Slack",
                    "{\n"
                    + "  \"type\" : \"SlackEventData\"\n"
                    + "}"
                });
    }

    @Test
    public void testEmailSenderHelper_directMentionsInMessage_replaceInEmailBody()
            throws IOException, MessagingException {
        EmailSenderHelper helper = new EmailSenderHelper("recipient@user.com", "bot@stakhanov.com", slackUserDataProvider);
        SlackStandardEvent input = (SlackStandardEvent)new ObjectMapper().readValue(
                "{"
                + "\"token\":\"mytoken\","
                + "\"event\": {"
                + "   \"type\":\"message\","
                + "   \"text\":\"what do you think of the very very very long monologue that they have given <@mnopqr>?\","
                + "   \"user\":\"abcdef\","
                + "   \"channel\":\"ghijkl\","
                + "   \"channel_type\":\"im\""
                + "},"
                + "\"type\":\"event_callback\""
                + "}",
                SlackEventPayload.class);
        MimeMessage emptyMimeMessage = new MimeMessage((Session)null);

        helper.setupMimeMessageToSend(emptyMimeMessage, input);

        assertArrayEquals(
                new Object[] {
                    new Address[] {new InternetAddress("recipient@user.com")},
                    new Address[] {new InternetAddress("bot+person.john.doe.abcdef@stakhanov.com", "John W. Doe")},
                    "what do you think of the very very very long monologue that ...",
                    "what do you think of the very very very long monologue that they have given Lionel?"
                },
                new Object[] {
                    emptyMimeMessage.getAllRecipients(),
                    emptyMimeMessage.getFrom(),
                    emptyMimeMessage.getSubject(),
                    emptyMimeMessage.getContent()
                });
    }

    @Test
    public void testEmailSenderHelper_directMentionsInMessageStart_replaceInSubject()
            throws IOException, MessagingException {
        EmailSenderHelper helper = new EmailSenderHelper("recipient@user.com", "bot@stakhanov.com", slackUserDataProvider);
        SlackStandardEvent input = (SlackStandardEvent)new ObjectMapper().readValue(
                "{"
                + "\"token\":\"mytoken\","
                + "\"event\": {"
                + "   \"type\":\"message\","
                + "   \"text\":\"what do you think <@mnopqr>?\","
                + "   \"user\":\"abcdef\","
                + "   \"channel\":\"ghijkl\","
                + "   \"channel_type\":\"im\""
                + "},"
                + "\"type\":\"event_callback\""
                + "}",
                SlackEventPayload.class);
        MimeMessage emptyMimeMessage = new MimeMessage((Session)null);

        helper.setupMimeMessageToSend(emptyMimeMessage, input);

        assertArrayEquals(
                new Object[] {
                    new Address[] {new InternetAddress("recipient@user.com")},
                    new Address[] {new InternetAddress("bot+person.john.doe.abcdef@stakhanov.com", "John W. Doe")},
                    "what do you think Lionel?",
                    "what do you think Lionel?"
                },
                new Object[] {
                    emptyMimeMessage.getAllRecipients(),
                    emptyMimeMessage.getFrom(),
                    emptyMimeMessage.getSubject(),
                    emptyMimeMessage.getContent()
                });
    }

    @Test
    public void testEmailSenderHelper_twoDirectMentionsSameFirstName_useFullNames()
            throws IOException, MessagingException {
        EmailSenderHelper helper = new EmailSenderHelper("recipient@user.com", "bot@stakhanov.com", slackUserDataProvider);
        SlackStandardEvent input = (SlackStandardEvent)new ObjectMapper().readValue(
                "{"
                + "\"token\":\"mytoken\","
                + "\"event\": {"
                + "   \"type\":\"message\","
                + "   \"text\":\"I want <@mnopqr> to do some work and <@stuvw> to review it\","
                + "   \"user\":\"abcdef\","
                + "   \"channel\":\"ghijkl\","
                + "   \"channel_type\":\"im\""
                + "},"
                + "\"type\":\"event_callback\""
                + "}",
                SlackEventPayload.class);
        MimeMessage emptyMimeMessage = new MimeMessage((Session)null);

        helper.setupMimeMessageToSend(emptyMimeMessage, input);

        assertArrayEquals(
                new Object[] {
                    new Address[] {new InternetAddress("recipient@user.com")},
                    new Address[] {new InternetAddress("bot+person.john.doe.abcdef@stakhanov.com", "John W. Doe")},
                    "I want Dr Lionel Logue to do some work and Lionel Bouchet to...",
                    "I want Dr Lionel Logue to do some work and Lionel Bouchet to review it"
                },
                new Object[] {
                    emptyMimeMessage.getAllRecipients(),
                    emptyMimeMessage.getFrom(),
                    emptyMimeMessage.getSubject(),
                    emptyMimeMessage.getContent()
                });
    }

    @Test
    public void testEmailSenderHelper_directMentionOfUserWithoutFirstName_returnCorrectValue()
            throws IOException, MessagingException {
        EmailSenderHelper helper = new EmailSenderHelper("recipient@user.com", "bot@stakhanov.com", slackUserDataProvider);
        SlackStandardEvent input = (SlackStandardEvent)new ObjectMapper().readValue(
                "{"
                + "\"token\":\"mytoken\","
                + "\"event\": {"
                + "   \"type\":\"message\","
                + "   \"text\":\"what do you think of the very very very long monologue "
                + "that they have given <@userWithoutFirstName>?\","
                + "   \"user\":\"abcdef\","
                + "   \"channel\":\"ghijkl\","
                + "   \"channel_type\":\"im\""
                + "},"
                + "\"type\":\"event_callback\""
                + "}",
                SlackEventPayload.class);
        MimeMessage emptyMimeMessage = new MimeMessage((Session)null);

        helper.setupMimeMessageToSend(emptyMimeMessage, input);

        assertArrayEquals(
                new Object[] {
                    new Address[] {new InternetAddress("recipient@user.com")},
                    new Address[] {new InternetAddress("bot+person.john.doe.abcdef@stakhanov.com", "John W. Doe")},
                    "what do you think of the very very very long monologue that ...",
                    "what do you think of the very very very long monologue that they have given Joe Dalton?"
                },
                new Object[] {
                    emptyMimeMessage.getAllRecipients(),
                    emptyMimeMessage.getFrom(),
                    emptyMimeMessage.getSubject(),
                    emptyMimeMessage.getContent()
                });
    }

    @Test
    public void testEmailSenderHelper_directMentionOfUserWithoutFullName_returnCorrectValue()
            throws IOException, MessagingException {
        EmailSenderHelper helper = new EmailSenderHelper("recipient@user.com", "bot@stakhanov.com", slackUserDataProvider);
        SlackStandardEvent input = (SlackStandardEvent)new ObjectMapper().readValue(
                "{"
                + "\"token\":\"mytoken\","
                + "\"event\": {"
                + "   \"type\":\"message\","
                + "   \"text\":\"what do you think of the very very very long monologue "
                + "that they have given <@userWithEmptyProfile>?\","
                + "   \"user\":\"abcdef\","
                + "   \"channel\":\"ghijkl\","
                + "   \"channel_type\":\"im\""
                + "},"
                + "\"type\":\"event_callback\""
                + "}",
                SlackEventPayload.class);
        MimeMessage emptyMimeMessage = new MimeMessage((Session)null);

        helper.setupMimeMessageToSend(emptyMimeMessage, input);

        assertArrayEquals(
                new Object[] {
                    new Address[] {new InternetAddress("recipient@user.com")},
                    new Address[] {new InternetAddress("bot+person.john.doe.abcdef@stakhanov.com", "John W. Doe")},
                    "what do you think of the very very very long monologue that ...",
                    "what do you think of the very very very long monologue that they have given jack.dalton?"
                },
                new Object[] {
                    emptyMimeMessage.getAllRecipients(),
                    emptyMimeMessage.getFrom(),
                    emptyMimeMessage.getSubject(),
                    emptyMimeMessage.getContent()
                });
    }
}
