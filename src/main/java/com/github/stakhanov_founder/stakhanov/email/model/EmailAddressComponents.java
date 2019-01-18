package com.github.stakhanov_founder.stakhanov.email.model;

public class EmailAddressComponents {

    public final String userName;
    public final String tag;
    public final String domain;

    public EmailAddressComponents(String userName, String tag, String domain) {
        this.userName = userName;
        this.tag = tag;
        this.domain = domain;
    }
}
