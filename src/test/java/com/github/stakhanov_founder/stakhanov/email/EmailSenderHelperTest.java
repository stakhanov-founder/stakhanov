package com.github.stakhanov_founder.stakhanov.email;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stakhanov_founder.stakhanov.dataproviders.SlackThreadMetadataSocket;
import com.github.stakhanov_founder.stakhanov.model.SlackStandardEvent;
import com.github.stakhanov_founder.stakhanov.slack.dataproviders.SlackChannelDataProvider;
import com.github.stakhanov_founder.stakhanov.slack.dataproviders.SlackGroupDataProvider;
import com.github.stakhanov_founder.stakhanov.slack.dataproviders.SlackUserDataProvider;
import com.github.stakhanov_founder.stakhanov.slack.eventreceiver.SlackEventPayload;

import allbegray.slack.type.Channel;
import allbegray.slack.type.Group;
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

    private SlackChannelDataProvider emptySlackChannelDataProvider = channelId -> Optional.empty();

    private SlackGroupDataProvider emptySlackGroupDataProvider = groupId -> Optional.empty();

    private SlackThreadMetadataSocket slackThreadMetadataSocket = new SlackThreadMetadataSocket() {
        @Override
        public Optional<String> getThreadSubjectForEmail(String channelId, double threadTimestampId) {
            return Optional.of("This is the thread starter's subject");
        }
        @Override
        public void setThreadSubjectForEmail(String channelId, double threadTimestampId, String subjectForEmail) { }
    };

    @Test
    public void testEmailSenderHelper_simpleMessage_returnCorrectValue()
            throws JsonParseException, JsonMappingException, IOException, MessagingException {
        EmailSenderHelper helper = new EmailSenderHelper(
                "mainUserSlackId", "recipient@user.com", "bot@stakhanov.com", slackUserDataProvider, null,
                emptySlackChannelDataProvider, emptySlackGroupDataProvider, slackThreadMetadataSocket);
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
        EmailSenderHelper helper = new EmailSenderHelper(
                "mainUserSlackId", "recipient@user.com", "bot@stakhanov.com", slackUserDataProvider, null,
                emptySlackChannelDataProvider, emptySlackGroupDataProvider, slackThreadMetadataSocket);
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
        EmailSenderHelper helper = new EmailSenderHelper(
                "mainUserSlackId", "recipient@user.com", "bot@stakhanov.com", slackUserDataProvider, null,
                emptySlackChannelDataProvider, emptySlackGroupDataProvider, slackThreadMetadataSocket);
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
        EmailSenderHelper helper = new EmailSenderHelper(
                "mainUserSlackId", "recipient@user.com", "bot@stakhanov.com", slackUserDataProvider, null,
                emptySlackChannelDataProvider, emptySlackGroupDataProvider, slackThreadMetadataSocket);
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
        EmailSenderHelper helper = new EmailSenderHelper(
                "mainUserSlackId", "recipient@user.com", "bot@stakhanov.com", slackUserDataProvider, null,
                emptySlackChannelDataProvider, emptySlackGroupDataProvider, slackThreadMetadataSocket);
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
        EmailSenderHelper helper = new EmailSenderHelper(
                "mainUserSlackId", "recipient@user.com", "bot@stakhanov.com", slackUserDataProvider, null,
                emptySlackChannelDataProvider, emptySlackGroupDataProvider, slackThreadMetadataSocket);
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
        EmailSenderHelper helper = new EmailSenderHelper(
                "mainUserSlackId", "recipient@user.com", "bot@stakhanov.com", slackUserDataProvider, null,
                emptySlackChannelDataProvider, emptySlackGroupDataProvider, slackThreadMetadataSocket);
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
        EmailSenderHelper helper = new EmailSenderHelper(
                "mainUserSlackId", "recipient@user.com", "bot@stakhanov.com", slackUserDataProvider, null,
                emptySlackChannelDataProvider, emptySlackGroupDataProvider, slackThreadMetadataSocket);
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

    @Test
    public void testEmailSenderHelper_simpleMessage_setCorrectMessageId()
            throws JsonParseException, JsonMappingException, IOException, MessagingException {
        EmailSenderHelper helper = new EmailSenderHelper(
                "mainUserSlackId", "recipient@user.com", "bot@stakhanov.com", slackUserDataProvider, null,
                emptySlackChannelDataProvider, emptySlackGroupDataProvider, slackThreadMetadataSocket);
        SlackStandardEvent input = (SlackStandardEvent)new ObjectMapper().readValue(
                "{"
                + "\"token\":\"mytoken\","
                + "\"event\": {"
                + "   \"type\":\"message\","
                + "   \"text\":\"this is my text\","
                + "   \"user\":\"abcdef\","
                + "   \"channel\":\"ghijkl\","
                + "   \"channel_type\":\"im\","
                + "   \"ts\":\"1546018211.000200\""
                + "},"
                + "\"type\":\"event_callback\""
                + "}",
                SlackEventPayload.class);
        MimeMessage emptyMimeMessage = new MimeMessage((Session)null);

        helper.setupMimeMessageToSend(emptyMimeMessage, input);

        assertEquals(
                "<slack.message.defaultworkspace.ghijkl.1546018211000200@stakhanov.stakhanov_founder.github.com>",
                emptyMimeMessage.getMessageID());
    }

    @Test
    public void testEmailSenderHelper_replyMessage_setCorrectInReplyToHeader()
            throws JsonParseException, JsonMappingException, IOException, MessagingException {
        EmailSenderHelper helper = new EmailSenderHelper(
                "mainUserSlackId", "recipient@user.com", "bot@stakhanov.com", slackUserDataProvider, null,
                emptySlackChannelDataProvider, emptySlackGroupDataProvider, slackThreadMetadataSocket);
        SlackStandardEvent input = (SlackStandardEvent)new ObjectMapper().readValue(
                "{"
                + "\"token\":\"mytoken\","
                + "\"event\": {"
                + "   \"type\":\"message\","
                + "   \"text\":\"this is my text\","
                + "   \"user\":\"abcdef\","
                + "   \"channel\":\"ghijkl\","
                + "   \"channel_type\":\"im\","
                + "   \"ts\":\"1546018211.000200\","
                + "   \"thread_ts\":\"1546018210.000500\""
                + "},"
                + "\"type\":\"event_callback\""
                + "}",
                SlackEventPayload.class);
        MimeMessage emptyMimeMessage = new MimeMessage((Session)null);

        helper.setupMimeMessageToSend(emptyMimeMessage, input);

        assertArrayEquals(
                new String[] { "<slack.message.defaultworkspace.ghijkl.1546018210000500@stakhanov.stakhanov_founder.github.com>" },
                emptyMimeMessage.getHeader("In-Reply-To"));
    }

    @Test
    public void testEmailSenderHelper_replyMessage_setSubjectOfThreadStarter()
            throws JsonParseException, JsonMappingException, IOException, MessagingException {
        EmailSenderHelper helper = new EmailSenderHelper(
                "mainUserSlackId", "recipient@user.com", "bot@stakhanov.com", slackUserDataProvider, null,
                emptySlackChannelDataProvider, emptySlackGroupDataProvider, slackThreadMetadataSocket);
        SlackStandardEvent input = (SlackStandardEvent)new ObjectMapper().readValue(
                "{"
                + "\"token\":\"mytoken\","
                + "\"event\": {"
                + "   \"type\":\"message\","
                + "   \"text\":\"this is my text\","
                + "   \"user\":\"abcdef\","
                + "   \"channel\":\"ghijkl\","
                + "   \"channel_type\":\"im\","
                + "   \"ts\":\"1546018211.000200\","
                + "   \"thread_ts\":\"1546018210.000500\""
                + "},"
                + "\"type\":\"event_callback\""
                + "}",
                SlackEventPayload.class);
        MimeMessage emptyMimeMessage = new MimeMessage((Session)null);

        helper.setupMimeMessageToSend(emptyMimeMessage, input);

        assertEquals(
                "Re: This is the thread starter's subject",
                emptyMimeMessage.getSubject());
    }

    @Test
    public void testEmailSenderHelper_replyMessageWithoutThreadData_setSubjectOfThreadStarter()
            throws JsonParseException, JsonMappingException, IOException, MessagingException {
        SlackThreadMetadataSocket emptyThreadMetadataSocket = new SlackThreadMetadataSocket() {
            @Override
            public Optional<String> getThreadSubjectForEmail(String channelId, double threadTimestampId) {
                return Optional.empty();
            }
            @Override
            public void setThreadSubjectForEmail(String channelId, double threadTimestampId, String subjectForEmail) { }
        };
        EmailSenderHelper helper = new EmailSenderHelper(
                "mainUserSlackId",
                "recipient@user.com",
                "bot@stakhanov.com",
                slackUserDataProvider,
                (channelId, timestampId) -> {
                    allbegray.slack.type.Message slackMessage = new allbegray.slack.type.Message();
                    slackMessage.setText("what do you think <@mnopqr>?");
                    return Optional.of(slackMessage);
                },
                null,
                emptySlackGroupDataProvider,
                emptyThreadMetadataSocket);
        SlackStandardEvent input = (SlackStandardEvent)new ObjectMapper().readValue(
                "{"
                + "\"token\":\"mytoken\","
                + "\"event\": {"
                + "   \"type\":\"message\","
                + "   \"text\":\"this is my text\","
                + "   \"user\":\"abcdef\","
                + "   \"channel\":\"ghijkl\","
                + "   \"channel_type\":\"im\","
                + "   \"ts\":\"1546018211.000200\","
                + "   \"thread_ts\":\"1546018210.000500\""
                + "},"
                + "\"type\":\"event_callback\""
                + "}",
                SlackEventPayload.class);
        MimeMessage emptyMimeMessage = new MimeMessage((Session)null);

        helper.setupMimeMessageToSend(emptyMimeMessage, input);

        assertEquals(
                "Re: what do you think Lionel?",
                emptyMimeMessage.getSubject());
    }

    @Test
    public void testEmailSenderHelper_simpleMessage_sendToChannelEmailAddress()
            throws IOException, MessagingException {

        SlackChannelDataProvider slackChannelDataProvider = channelId -> {
            Channel channel = new Channel();
            channel.setName("mychannel");
            return Optional.of(channel);
        };
        EmailSenderHelper helper = new EmailSenderHelper(
                "mainUserSlackId", "recipient@user.com", "bot@stakhanov.com", slackUserDataProvider, null,
                slackChannelDataProvider, emptySlackGroupDataProvider, slackThreadMetadataSocket);
        SlackStandardEvent input = (SlackStandardEvent)new ObjectMapper().readValue(
                "{"
                + "\"token\":\"mytoken\","
                + "\"event\": {"
                + "   \"type\":\"message\","
                + "   \"text\":\"this is my text\","
                + "   \"user\":\"abcdef\","
                + "   \"channel\":\"ghijkl\","
                + "   \"channel_type\":\"channel\""
                + "},"
                + "\"type\":\"event_callback\""
                + "}",
                SlackEventPayload.class);
        MimeMessage emptyMimeMessage = new MimeMessage((Session)null);

        helper.setupMimeMessageToSend(emptyMimeMessage, input);

        assertArrayEquals(
                new Address[] { new InternetAddress("bot+channel.mychannel.ghijkl@stakhanov.com", "#mychannel") },
                emptyMimeMessage.getRecipients(Message.RecipientType.TO));
    }

    @Test
    public void testEmailSenderHelper_simpleMessage_mainUserIsInBcc()
            throws IOException, MessagingException {
        EmailSenderHelper helper = new EmailSenderHelper(
                "mainUserSlackId", "recipient@user.com", "bot@stakhanov.com", slackUserDataProvider, null,
                emptySlackChannelDataProvider, emptySlackGroupDataProvider, slackThreadMetadataSocket);
        SlackStandardEvent input = (SlackStandardEvent)new ObjectMapper().readValue(
                "{"
                + "\"token\":\"mytoken\","
                + "\"event\": {"
                + "   \"type\":\"message\","
                + "   \"text\":\"this is my text\","
                + "   \"user\":\"abcdef\","
                + "   \"channel\":\"ghijkl\","
                + "   \"channel_type\":\"channel\""
                + "},"
                + "\"type\":\"event_callback\""
                + "}",
                SlackEventPayload.class);
        MimeMessage emptyMimeMessage = new MimeMessage((Session)null);

        helper.setupMimeMessageToSend(emptyMimeMessage, input);

        assertThat(Arrays.asList(emptyMimeMessage.getRecipients(Message.RecipientType.BCC)),
                hasItem(new InternetAddress("recipient@user.com")));
    }

    @Test
    public void testEmailSenderHelper_groupMessage_allMembersButSenderAreRecipients()
            throws IOException, MessagingException {
        SlackGroupDataProvider slackGroupDataProvider = groupId -> {
            Group group = new Group();
            group.setMembers(Arrays.asList(
                    "abcdef",
                    "stuvw",
                    "userWithoutFirstName"));
            return Optional.of(group);
        };
        EmailSenderHelper helper = new EmailSenderHelper(
                "stuvw", "recipient@user.com", "bot@stakhanov.com", slackUserDataProvider, null,
                emptySlackChannelDataProvider, slackGroupDataProvider, slackThreadMetadataSocket);
        SlackStandardEvent input = (SlackStandardEvent)new ObjectMapper().readValue(
                "{"
                + "\"token\":\"mytoken\","
                + "\"event\": {"
                + "   \"type\":\"message\","
                + "   \"text\":\"this is my text\","
                + "   \"user\":\"abcdef\","
                + "   \"channel\":\"bbbbbb\","
                + "   \"channel_type\":\"mpim\""
                + "},"
                + "\"type\":\"event_callback\""
                + "}",
                SlackEventPayload.class);
        MimeMessage emptyMimeMessage = new MimeMessage((Session)null);

        helper.setupMimeMessageToSend(emptyMimeMessage, input);

        assertArrayEquals(
                new Address[] {
                        new InternetAddress("recipient@user.com"),
                        new InternetAddress("bot+person.joe.dalton.userWithoutFirstName@stakhanov.com", "joe.dalton")
                },
                emptyMimeMessage.getRecipients(Message.RecipientType.TO));
    }

    @Test
    public void testEmailSenderHelper_groupMessage_mainUserIsNotCced()
            throws IOException, MessagingException {
        SlackGroupDataProvider slackGroupDataProvider = groupId -> {
            Group group = new Group();
            group.setMembers(Arrays.asList(
                    "abcdef",
                    "stuvw",
                    "userWithoutFirstName"));
            return Optional.of(group);
        };
        EmailSenderHelper helper = new EmailSenderHelper(
                "stuvw", "recipient@user.com", "bot@stakhanov.com", slackUserDataProvider, null,
                emptySlackChannelDataProvider, slackGroupDataProvider, slackThreadMetadataSocket);
        SlackStandardEvent input = (SlackStandardEvent)new ObjectMapper().readValue(
                "{"
                + "\"token\":\"mytoken\","
                + "\"event\": {"
                + "   \"type\":\"message\","
                + "   \"text\":\"this is my text\","
                + "   \"user\":\"abcdef\","
                + "   \"channel\":\"bbbbbb\","
                + "   \"channel_type\":\"mpim\""
                + "},"
                + "\"type\":\"event_callback\""
                + "}",
                SlackEventPayload.class);
        MimeMessage emptyMimeMessage = new MimeMessage((Session)null);

        helper.setupMimeMessageToSend(emptyMimeMessage, input);

        assertNull(emptyMimeMessage.getRecipients(Message.RecipientType.CC));
    }
}
