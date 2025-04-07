package backend.academy.bot.repository;

import backend.academy.bot.model.UserState;
import java.util.Map;

public interface StateRepository {
    void setState(Long id, UserState state);

    UserState getStateById(Long id);

    void initUserState(Map<Long, UserState> map);
}
