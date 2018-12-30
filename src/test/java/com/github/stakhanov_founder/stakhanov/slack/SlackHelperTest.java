package com.github.stakhanov_founder.stakhanov.slack;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class SlackHelperTest {

    @Test
    public void testExtractDirectMentions_baseCase_returnCorrectValue() {
        String input = "How are you <@UF301RQS2>?";
        SlackHelper helper = new SlackHelper();

        List<MentionLocation> result = helper.extractDirectMentions(input);

        assertEquals(
                Arrays.asList(new MentionLocation(12, 12, "UF301RQS2")),
                result);
    }

    @Test
    public void testExtractDirectMentions_severalMentions_listAll() {
        String input = "How are you <@UF301RQS2> and <@whatever>?";
        SlackHelper helper = new SlackHelper();

        List<MentionLocation> result = helper.extractDirectMentions(input);

        assertEquals(
                Arrays.asList(
                        new MentionLocation(12, 12, "UF301RQS2"),
                        new MentionLocation(29, 11, "whatever")),
                result);
    }

    @Test
    public void testExtractDirectMentions_noMention_returnEmptyList() {
        String input = "How are you man?";
        SlackHelper helper = new SlackHelper();

        List<MentionLocation> result = helper.extractDirectMentions(input);

        assertEquals(
                Collections.EMPTY_LIST,
                result);
    }

    @Test
    public void testExtractDirectMentions_otherHtmlTag_ignoreIt() {
        String input = "How are you <bold><@UF301RQS2></bold>?";
        SlackHelper helper = new SlackHelper();

        List<MentionLocation> result = helper.extractDirectMentions(input);

        assertEquals(
                Arrays.asList(new MentionLocation(18, 12, "UF301RQS2")),
                result);
    }
}
