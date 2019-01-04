CREATE TABLE IF NOT EXISTS slack_thread_metadata (
    channel_id VARCHAR NOT NULL,
    thread_timestamp_id DOUBLE PRECISION NOT NULL,
    email_subject VARCHAR NOT NULL,
    UNIQUE (channel_id, thread_timestamp_id)
)
