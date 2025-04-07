package backend.academy.scrapper.repository.jdbc;

import backend.academy.scrapper.repository.TelegramChatRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
@Repository
public class TelegramChatRepositoryJDBC implements TelegramChatRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public TelegramChatRepositoryJDBC(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveChat(Long tgChatId) {
        String sql = "INSERT INTO users (chat_id) VALUES (:chatId)";
        MapSqlParameterSource params = new MapSqlParameterSource("chatId", tgChatId);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public void removeChat(Long tgChatId) {
        String sql = "DELETE FROM users WHERE chat_id = :chatId";
        MapSqlParameterSource params = new MapSqlParameterSource("chatId", tgChatId);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public Integer countChatId(Long tgChatId) {
        String sql = "SELECT COUNT(*) FROM users WHERE chat_id = :chatId";
        MapSqlParameterSource params = new MapSqlParameterSource("chatId", tgChatId);
        return jdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<Long> findAllUsers() {
        String sql = "SELECT u.chat_id FROM Users u";
        return jdbcTemplate.query(sql, new MapSqlParameterSource(), SingleColumnRowMapper.newInstance(Long.class));
    }
}
