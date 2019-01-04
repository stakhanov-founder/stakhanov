package com.github.stakhanov_founder.stakhanov.dataproviders;

import java.io.IOException;
import java.util.Optional;

public interface SlackThreadMetadataSocket {

    Optional<String> getThreadSubjectForEmail(String channelId, double threadTimestampId);

    void setThreadSubjectForEmail(String channelId, double threadTimestampId, String subjectForEmail) throws IOException;
}
