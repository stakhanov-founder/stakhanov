package com.github.stakhanov_founder.stakhanov.slack.eventreceiver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class SlackEventReceiverResourceTest {

	@Test
	public void testPost_urlVerification_returnCorrectValue()
			throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		SlackEventReceiverResource resource = new SlackEventReceiverResource(event -> {});
		String rawPayload = "{\"type\":\"url_verification\", \"challenge\": \"abcdefgh\", \"token\":\"xyz\"}";

		String rawResult = new ObjectMapper().writeValueAsString(resource.handlePost(rawPayload));

		assertEquals(rawResult, "{\"challenge\":\"abcdefgh\"}");
	}

    @Test
    public void testPost_urlVerification_doNotEnqueueEvent()
            throws JsonParseException, JsonMappingException, IOException {
        SlackEventReceiverResource resource = new SlackEventReceiverResource(event -> fail());
        String rawPayload = "{\"type\":\"url_verification\", \"challenge\": \"abcdefgh\", \"token\":\"xyz\"}";

        resource.handlePost(rawPayload);
    }
}
