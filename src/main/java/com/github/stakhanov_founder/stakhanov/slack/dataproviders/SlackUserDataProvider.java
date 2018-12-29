package com.github.stakhanov_founder.stakhanov.slack.dataproviders;

import allbegray.slack.type.User;

public interface SlackUserDataProvider {

    User getUser(String id);

}
