package backend.academy.bot.repository.impl;

import backend.academy.bot.model.UserState;
import backend.academy.bot.repository.StateRepository;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StateRepositoryImpl implements StateRepository {
    private Map<Long, UserState> usersStates = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        usersStates = new HashMap<>();
    }

    @Override
    public void setState(Long id, UserState state) {
        usersStates.put(id, state);
    }

    @Override
    public UserState getStateById(Long id) {
        return usersStates.get(id);
    }

    @Override
    public void initUserState(Map<Long, UserState> map) {
        if (map == null) {
            throw new IllegalArgumentException("Empty user states are not allow");
        }
        usersStates.clear();
        usersStates.putAll(map);
    }
}
