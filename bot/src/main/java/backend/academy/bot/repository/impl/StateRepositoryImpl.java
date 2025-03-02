package backend.academy.bot.repository.impl;

import backend.academy.bot.model.UserState;
import backend.academy.bot.repository.StateRepository;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class StateRepositoryImpl implements StateRepository {
    private final Map<Long, UserState> usersStates = new HashMap<>();

    @Override
    public void setState(Long id, UserState state) {
        usersStates.put(id, state);
    }

    @Override
    public UserState getStateById(Long id) {
        return usersStates.get(id);
    }
}
