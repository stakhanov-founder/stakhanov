package com.github.stakhanov_founder.stakhanov.email;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.stakhanov_founder.stakhanov.email.microsoftsdk.MicrosoftApiAuthenticator;
import com.github.stakhanov_founder.stakhanov.email.microsoftsdk.MicrosoftEmailMessage;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.Message;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.graph.requests.extensions.IMessageCollectionPage;

public class EmailReceiver extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(EmailReceiver.class);

    private final IGraphServiceClient microsoftGraphClient;
    private final EmailReceiverHelper helper;
    private final Message emailMessageToMarkAsRead;

    public EmailReceiver(String credentials) throws UnsupportedEncodingException {
        String[] splitCredentials = credentials.split("/");
        if (splitCredentials.length != 4) {
            throw new IllegalArgumentException(
                    "Wrong format of credentials. Expected <app-id>/<app-secret>/<refresh-token>/<url-encoded-redirect-uri>");
        }
        microsoftGraphClient = GraphServiceClient
                .builder().authenticationProvider(new MicrosoftApiAuthenticator(splitCredentials[0],
                        splitCredentials[1], splitCredentials[2], URLDecoder.decode(splitCredentials[3], "UTF-8")))
                .buildClient();
        helper = new EmailReceiverHelper();
        emailMessageToMarkAsRead = new Message();
        emailMessageToMarkAsRead.isRead = true;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                IMessageCollectionPage messages = microsoftGraphClient
                        .me()
                        .mailFolders("Inbox")
                        .messages()
                        .buildRequest(Arrays.asList(
                                new QueryOption("$filter", "isRead eq false"),
                                new QueryOption("$orderby", "receivedDateTime asc")))
                        .get();
                if (messages != null) {
                    for (Message message : messages.getCurrentPage()) {
                        try {
                            helper.processEmail(new MicrosoftEmailMessage(message));
                            microsoftGraphClient
                                .me()
                                .mailFolders("Inbox")
                                .messages(message.id)
                                .buildRequest()
                                .patch(emailMessageToMarkAsRead);
                        }
                        catch (Exception ex) {
                            logger.error("Exception occurred while processing incoming email", ex);
                        }
                    }
                }
                Thread.sleep(150);
            } catch (InterruptedException ex) {
                break;
            }
        }
    }
}
