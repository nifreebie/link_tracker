package backend.academy.bot.model;

public enum UserState {
    DEFAULT,
    AWAITING_TRACK_URL,
    AWAITING_UNTRACK_URL,
    AWAITING_TAGS,
    AWAITING_FILTERS
}
