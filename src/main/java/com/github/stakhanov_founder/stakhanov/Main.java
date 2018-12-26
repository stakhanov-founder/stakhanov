package com.github.stakhanov_founder.stakhanov;

import com.github.stakhanov_founder.stakhanov.slack.eventreceiver.SlackEventReceiverApplication;

public class Main {

	public static void main(String[] args) throws Exception {
        new SlackEventReceiverApplication().run("server",
                "com/github/stakhanov_founder/stakhanov/slack/eventreceiver/configuration.yml");
    }
}
