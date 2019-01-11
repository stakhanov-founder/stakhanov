package com.github.stakhanov_founder.stakhanov.email.microsoftsdk;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.http.IHttpRequest;

public class MicrosoftApiAuthenticator implements IAuthenticationProvider {

    private static final String TOKEN_REQUEST_URL = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
    private static final String OAUTH_SCOPE = "user.read mail.readwrite offline_access";

    private final String appId;
    private final String appSecret;
    private final String refreshToken;
    private final String redirectUri;

    private long tokenValidityLimit = Instant.now().getEpochSecond();
    private String token = null;

    public MicrosoftApiAuthenticator(String appId, String appSecret, String refreshToken, String redirectUri) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.refreshToken = refreshToken;
        this.redirectUri = redirectUri;
    }

    @Override
    public void authenticateRequest(IHttpRequest request) {
        try {
            if (Instant.now().getEpochSecond() + 60 >= tokenValidityLimit) {
                getNewToken();
            }
            request.addHeader("Authorization", "Bearer " + token);
        } catch (IOException ex) {
            throw new RuntimeException("Could not get a token from Microsoft", ex);
        }
    }

    private void getNewToken() throws IOException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(TOKEN_REQUEST_URL);

        List<NameValuePair> params = Arrays.asList(
                new BasicNameValuePair("client_id", appId),
                new BasicNameValuePair("scope", OAUTH_SCOPE),
                new BasicNameValuePair("refresh_token", refreshToken),
                new BasicNameValuePair("redirect_uri", redirectUri),
                new BasicNameValuePair("grant_type", "refresh_token"),
                new BasicNameValuePair("client_secret", appSecret));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        if (entity == null) {
            throw new RuntimeException("Could not get token from Microsoft: " + response.toString());
        }
        try (InputStream entityContentInputStream = entity.getContent()) {
            MicrosoftApiTokenResponse responseContent = new ObjectMapper().readValue(entityContentInputStream,
                    MicrosoftApiTokenResponse.class);
            token = responseContent.accessToken;
            tokenValidityLimit = Instant.now().getEpochSecond() + responseContent.validity;
        }
    }
}
