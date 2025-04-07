package backend.academy.scrapper.repository.jdbc;

import backend.academy.scrapper.exceptions.NotFoundException;
import backend.academy.scrapper.model.dto.TagDTO;
import backend.academy.scrapper.repository.TagRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
@Repository
public class TagRepositoryJDBC implements TagRepository {

    private static class Mapper implements RowMapper<TagDTO> {
        @Override
        public TagDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            long telegramChatId = rs.getLong("user_id");
            return new TagDTO(id, name, telegramChatId, List.of());
        }
    }

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public TagRepositoryJDBC(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String createTag(String name, Long telegramChatId) {
        String userSql = "SELECT id FROM users WHERE chat_id = :chatId";
        MapSqlParameterSource userParams = new MapSqlParameterSource("chatId", telegramChatId);
        Long userId = jdbcTemplate.queryForObject(userSql, userParams, Long.class);

        String sql = "INSERT INTO tags (name, user_id) VALUES (:name, :userId) RETURNING id, name, user_id";
        MapSqlParameterSource params =
                new MapSqlParameterSource().addValue("name", name).addValue("userId", userId);
        TagDTO tagDTO = jdbcTemplate.queryForObject(sql, params, new Mapper());
        if (tagDTO != null) {
            return loadLinks(tagDTO).name();
        } else throw new NotFoundException("Тэг не найден");
    }

    @Override
    public List<String> getUserTags(Long telegramChatId) {
        String sql = "SELECT t.name FROM tags t " + "JOIN users u ON t.user_id = u.id " + "WHERE u.chat_id = :chatId";
        MapSqlParameterSource params = new MapSqlParameterSource("chatId", telegramChatId);
        return jdbcTemplate.queryForList(sql, params, String.class);
    }

    private TagDTO findTagByName(String name, Long telegramChatId) {
        String sql =
                """
                    SELECT t.id, t.name, t.user_id
                    FROM tags t
                    WHERE t.name = :name
                    AND EXISTS (
                        SELECT 1 FROM users u WHERE u.id = t.user_id AND u.chat_id = :chatId
                    )
                """;
        MapSqlParameterSource params =
                new MapSqlParameterSource().addValue("name", name).addValue("chatId", telegramChatId);
        TagDTO tagDTO = jdbcTemplate.queryForObject(sql, params, new Mapper());
        if (tagDTO != null) {
            return loadLinks(tagDTO);
        } else throw new NotFoundException("Тэг не найден");
    }

    @Override
    public void addLinkTag(String tagName, String url, Long telegramChatId) {
        TagDTO tagDTO = findTagByName(tagName, telegramChatId);

        String linkSql = "SELECT id FROM links WHERE url = :url";
        MapSqlParameterSource linkParams = new MapSqlParameterSource("url", url);
        Integer linkId = jdbcTemplate.queryForObject(linkSql, linkParams, Integer.class);

        String sql = "INSERT INTO link_tag (link_id, tag_id) " + "SELECT :linkId, :tagId "
                + "WHERE NOT EXISTS (SELECT 1 FROM link_tag WHERE link_id = :linkId AND tag_id = :tagId)";
        MapSqlParameterSource params =
                new MapSqlParameterSource().addValue("linkId", linkId).addValue("tagId", tagDTO.id());
        jdbcTemplate.update(sql, params);
    }

    @Override
    public void removeLinkTag(String tagName, String url, Long telegramChatId) {
        TagDTO tagDTO = findTagByName(tagName, telegramChatId);

        String linkSql = "SELECT id FROM links WHERE url = :url";
        MapSqlParameterSource linkParams = new MapSqlParameterSource("url", url);
        Integer linkId = jdbcTemplate.queryForObject(linkSql, linkParams, Integer.class);

        String sql = "DELETE FROM link_tag WHERE link_id = :linkId AND tag_id = :tagId";
        MapSqlParameterSource params =
                new MapSqlParameterSource().addValue("linkId", linkId).addValue("tagId", tagDTO.id());
        jdbcTemplate.update(sql, params);
    }

    @Override
    public boolean isTagExists(String name) {
        String sql = "SELECT COUNT(*) FROM tags WHERE name = :name";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", name);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        if (count == null) return false;
        return count > 0;
    }

    private TagDTO loadLinks(TagDTO tagDTO) {
        String sql =
                "SELECT l.url FROM links l " + "JOIN link_tag lt ON l.id = lt.link_id " + "WHERE lt.tag_id = :tagId";
        MapSqlParameterSource params = new MapSqlParameterSource("tagId", tagDTO.id());
        List<String> links = jdbcTemplate.queryForList(sql, params, String.class);
        return new TagDTO(tagDTO.id(), tagDTO.name(), tagDTO.telegramChatId(), links);
    }
}
