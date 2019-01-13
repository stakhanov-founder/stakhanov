package com.github.stakhanov_founder.stakhanov.slack;

import java.util.function.Supplier;

import allbegray.slack.webapi.SlackWebApiClient;

public class SlackOperator extends Thread {

    private final Supplier<SlackOperation> instructionGiver;
    private final SlackOperatorHelper helper;

    public SlackOperator(SlackWebApiClient slackClient, Supplier<SlackOperation> instructionGiver) {
        if (instructionGiver == null) {
            throw new IllegalArgumentException(
                    "Null passed as argument to " + SlackOperator.class.getSimpleName() + "'s constructor");
        }
        this.instructionGiver = instructionGiver;
        helper = new SlackOperatorHelper(slackClient);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                SlackOperation operationToCarryOut = instructionGiver.get();
                if (operationToCarryOut != null) {
                    helper.carryOutOperation(operationToCarryOut);
                }
                else {
                    Thread.sleep(100);
                }
            }
            catch (InterruptedException ex) {
                break;
            }
        }
    }
}
