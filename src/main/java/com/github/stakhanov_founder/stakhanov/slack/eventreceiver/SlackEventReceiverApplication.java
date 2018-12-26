package com.github.stakhanov_founder.stakhanov.slack.eventreceiver;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class SlackEventReceiverApplication extends Application<Configuration> {

    @Override
    public String getName() {
        return "Slack event receiver";
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(new ResourceConfigurationSourceProvider());
    }

    @Override
    public void run(Configuration configuration, Environment environment) {
        environment.jersey().register(new SlackEventReceiverResource());
    }
}
