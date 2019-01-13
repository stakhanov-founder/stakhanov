package com.github.stakhanov_founder.stakhanov.email.model;

import java.util.Optional;

public class SlackUserEmailTag implements EmailTag {

    public final Optional<String> userName;
    public final Optional<String> userId;

    public SlackUserEmailTag(Optional<String> userName, Optional<String> userId) {
        this.userName = userName;
        this.userId = userId;
    }
}
