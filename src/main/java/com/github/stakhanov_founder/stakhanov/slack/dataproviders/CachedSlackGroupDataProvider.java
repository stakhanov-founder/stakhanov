package com.github.stakhanov_founder.stakhanov.slack.dataproviders;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import allbegray.slack.type.Group;
import allbegray.slack.webapi.SlackWebApiClient;

public class CachedSlackGroupDataProvider implements SlackGroupDataProvider {

    private final SlackWebApiClient slackClient;
    private Instant lastFetchOfData;
    private final Map<String, Group> groupsById = new HashMap<>();

    public CachedSlackGroupDataProvider(SlackWebApiClient slackClient) {
        if (slackClient == null) {
            throw new IllegalArgumentException("Null passed as Slack client to "
                    + CachedSlackGroupDataProvider.class.getSimpleName() + "'s constructor");
        }
        this.slackClient = slackClient;
    }

    @Override
    public Optional<Group> getGroupDetails(String id) {
        Group group = groupsById.get(id);
        if (group == null || lastFetchOfData.plus(1, ChronoUnit.DAYS).isBefore(Instant.now())) {
            refreshData();
            group = groupsById.get(id);
            if (group == null) {
                return Optional.empty();
            }
        }
        return Optional.of(group);
    }

    private void refreshData() {
        groupsById.clear();
        lastFetchOfData = Instant.now();
        List<Group> groups = slackClient.getGroupList();
        for (Group group : groups) {
            groupsById.put(group.getId(), group);
        }
    }
}
