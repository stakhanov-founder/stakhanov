package com.github.stakhanov_founder.stakhanov.email.microsoftsdk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class MicrosoftApiTokenResponse {

    @JsonProperty("access_token")
    String accessToken;

    @JsonProperty("expires_in")
    int validity;
}
