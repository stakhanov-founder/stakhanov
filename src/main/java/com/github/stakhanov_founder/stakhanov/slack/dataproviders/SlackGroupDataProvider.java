package com.github.stakhanov_founder.stakhanov.slack.dataproviders;

import java.util.Optional;

import allbegray.slack.type.Group;

public interface SlackGroupDataProvider {

    Optional<Group> getGroupDetails(String groupId);
}
