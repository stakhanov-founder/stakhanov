package com.github.stakhanov_founder.stakhanov.dataproviders;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleSlackThreadMetadataSocket implements SlackThreadMetadataSocket {

    private static final Logger logger = LoggerFactory.getLogger(SimpleSlackThreadMetadataSocket.class);

    private final Connection databaseConnection;

    public SimpleSlackThreadMetadataSocket(Connection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public Optional<String> getThreadSubjectForEmail(String channelId, double threadTimestampId) {
        try {
            PreparedStatement preparedStatement = databaseConnection
                .prepareStatement("select * from slack_thread_metadata where channel_id = ? and thread_timestamp_id = ?");
            preparedStatement.setString(1, channelId);
            preparedStatement.setDouble(2, threadTimestampId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(resultSet.getString("email_subject"));
            }
        } catch (SQLException ex) {
            logger.error("An exception occurred while trying to fetch a slack thread subject for email from the database", ex);
        }
        return Optional.empty();
    }

    @Override
    public void setThreadSubjectForEmail(String channelId, double threadTimestampId, String subjectForEmail) throws IOException {
        Optional<String> stateOfDatabase = getThreadSubjectForEmail(channelId, threadTimestampId);
        PreparedStatement preparedStatement;
        try {
            if (stateOfDatabase.isPresent()) {
                preparedStatement = databaseConnection
                    .prepareStatement(
                        "update slack_thread_metadata set email_subject = ? where channel_id = ? and thread_timestamp_id = ?");
                preparedStatement.setString(1, subjectForEmail);
                preparedStatement.setString(2, channelId);
                preparedStatement.setDouble(3, threadTimestampId);
            }
            else {
                preparedStatement = databaseConnection
                    .prepareStatement(
                            "insert into slack_thread_metadata (channel_id, thread_timestamp_id, email_subject) values (?, ?, ?)");
                preparedStatement.setString(1, channelId);
                preparedStatement.setDouble(2, threadTimestampId);
                preparedStatement.setString(3, subjectForEmail);
            }
            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                throw new IOException("No rows were affected in database while trying to set email subject for a Slack message. "
                        + "Channel id: " + channelId + ", thread timestamp id: " + threadTimestampId
                        + ", subject: '" + subjectForEmail + "'");
            }
        }
        catch (SQLException ex) {
            throw new IOException("An error occurred while trying to set email subject for a Slack message", ex);
        }
    }
}
