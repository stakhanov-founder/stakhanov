package com.github.stakhanov_founder.stakhanov.slack;

import java.util.Objects;

public class MentionLocation {

    public final int start;
    public final int length;
    public final String id;

    public MentionLocation(int start, int length, String id) {
        this.start = start;
        this.length = length;
        this.id = id;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MentionLocation) || other == null) {
            return false;
        }
        MentionLocation otherLocation = (MentionLocation) other;
        return this.start == otherLocation.start && this.length == otherLocation.length
                && Objects.equals(this.id, otherLocation.id);
    }

    @Override
    public String toString() {
        return "(" + start + ", " + length + ", " + id + ")";
    }
}
