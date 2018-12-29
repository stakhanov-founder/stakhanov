package com.github.stakhanov_founder.stakhanov.slack.dataproviders;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import allbegray.slack.type.User;
import allbegray.slack.webapi.SlackWebApiClient;

public class CachedSlackUserDataProvider implements SlackUserDataProvider {

    private final SlackWebApiClient slackClient;
    private Instant lastFetchOfData;
    private final Map<String, User> usersById = new HashMap<>();

    public CachedSlackUserDataProvider(SlackWebApiClient slackClient) {
        if (slackClient == null) {
            throw new IllegalArgumentException("Null passed as Slack client to "
                    + CachedSlackUserDataProvider.class.getSimpleName() + "'s constructor");
        }
        this.slackClient = slackClient;
    }

    @Override
    public User getUser(String id) {
        User user = usersById.get(id);
        if (user == null || lastFetchOfData.plus(1, ChronoUnit.DAYS).isBefore(Instant.now())) {
            refreshData();
            user = usersById.get(id);
            if (user == null) {
                throw new NoSuchElementException("Could not find any user with id " + id);
            }
        }
        return user;
    }

    private void refreshData() {
        usersById.clear();
        lastFetchOfData = Instant.now();
        List<User> users = slackClient.getUserList();
        for (User user : users) {
            usersById.put(user.getId(), user);
        }
    }
}
