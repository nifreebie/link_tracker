package backend.academy.bot.repository;

import backend.academy.bot.model.UserState;

public interface StateRepository {
    void setState(Long id, UserState state);

    UserState getStateById(Long id);
}
