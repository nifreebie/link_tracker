package backend.academy.scrapper.repository.jdbc;

import backend.academy.scrapper.model.domain.LinkType;
import backend.academy.scrapper.model.dto.LinkDTO;
import backend.academy.scrapper.repository.LinkRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
@Repository
public class LinkRepositoryJDBC implements LinkRepository {

    private static class Mapper implements RowMapper<LinkDTO> {
        @Override
        public LinkDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            int id = rs.getInt("id");
            String url = rs.getString("url");
            LocalDateTime lastUpdated = rs.getTimestamp("last_updated_at").toLocalDateTime();
            LinkType linkType = LinkType.valueOf(rs.getString("type"));
            List<String> filters = rs.getString("filters") != null
                    ? List.of(rs.getString("filters").split(","))
                    : Collections.emptyList();
            return new LinkDTO(id, url, new ArrayList<>(), filters, lastUpdated, new ArrayList<>(), linkType);
        }
    }

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public LinkRepositoryJDBC(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    @Override
    public List<LinkDTO> findUserLinks(Long telegramChatId) {
        String sql = "SELECT l.* FROM links l " + "JOIN user_link ltc ON l.id = ltc.link_id "
                + "JOIN users u ON u.id = ltc.user_id "
                + "WHERE u.chat_id = :chatId";
        MapSqlParameterSource params = new MapSqlParameterSource("chatId", telegramChatId);
        return jdbcTemplate.query(sql, params, new Mapper());
    }

    @Transactional
    @Override
    public LinkDTO saveLink(String url, List<String> tags, List<String> filters, Long telegramChatId) {
        Long userId = findUserIdByChatId(telegramChatId);
        List<Long> tagIds = findTagIdsByNames(tags);

        LinkDTO linkDto;
        try {
            String selectLinkSql = "SELECT * FROM links WHERE url = :url";
            MapSqlParameterSource params = new MapSqlParameterSource("url", url);
            linkDto = jdbcTemplate.queryForObject(selectLinkSql, params, new Mapper());
            Integer linkId = null;
            if (linkDto != null) {
                linkId = linkDto.id();
            }
            addUserToLink(linkId, userId);
            addTagsToLink(linkId, tagIds);
        } catch (EmptyResultDataAccessException e) {
            Integer linkId = insertNewLink(url, filters);
            addUserToLink(linkId, userId);
            addTagsToLink(linkId, tagIds);
            linkDto = new LinkDTO(
                    linkId,
                    url,
                    tags,
                    filters,
                    LocalDateTime.now(ZoneOffset.UTC),
                    Collections.singletonList(userId),
                    LinkType.getLinkType(url));
        }
        return linkDto;
    }

    @Transactional
    @Override
    public List<LinkDTO> getAll() {
        String sql = "SELECT * FROM links";
        return jdbcTemplate.query(sql, new MapSqlParameterSource(), new Mapper());
    }

    @Transactional
    @Override
    public boolean isUrlExists(String url) {
        String sql = "SELECT COUNT(*) FROM links WHERE url = :url";
        MapSqlParameterSource params = new MapSqlParameterSource("url", url);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    @Transactional
    @Override
    public LinkDTO removeLinkByUrlAndTelegramChatId(String url, Long telegramChatId) {
        try {
            String selectLinkSql = "SELECT * FROM links WHERE url = :url";
            MapSqlParameterSource linkParams = new MapSqlParameterSource("url", url);
            LinkDTO linkDto = jdbcTemplate.queryForObject(selectLinkSql, linkParams, new Mapper());
            Integer linkId = null;
            if (linkDto != null) {
                linkId = linkDto.id();
            }
            Long userId = findUserIdByChatId(telegramChatId);
            String deleteSql = "DELETE FROM user_link WHERE link_id = :linkId AND user_id = :userId";
            MapSqlParameterSource deleteParams =
                    new MapSqlParameterSource().addValue("linkId", linkId).addValue("userId", userId);
            jdbcTemplate.update(deleteSql, deleteParams);
            return linkDto;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Transactional
    @Override
    public LinkDTO findLinkByUrl(String url) {
        String sql = "SELECT * FROM links WHERE url = :url";
        MapSqlParameterSource params = new MapSqlParameterSource("url", url);
        return jdbcTemplate.queryForObject(sql, params, new Mapper());
    }

    @Transactional
    @Override
    public void updateLastUpdatedAt(Integer id, LocalDateTime lastUpdatedAt) {
        String sql = "UPDATE links SET last_updated_at = :lastUpdatedAt WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("lastUpdatedAt", Timestamp.valueOf(lastUpdatedAt))
                .addValue("id", id);
        jdbcTemplate.update(sql, params);
    }

    @Override
    @Transactional
    public List<LinkDTO> getPaginatedLinks(Integer offset, Integer limit) {
        String sql = "SELECT * FROM links LIMIT :limit OFFSET :offset";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("limit", limit);
        params.addValue("offset", offset);
        return jdbcTemplate.query(sql, params, new Mapper());
    }

    private Long findUserIdByChatId(Long telegramChatId) {
        String sql = "SELECT id FROM users WHERE chat_id = :chatId";
        MapSqlParameterSource params = new MapSqlParameterSource("chatId", telegramChatId);
        return jdbcTemplate.queryForObject(sql, params, Long.class);
    }

    private List<Long> findTagIdsByNames(List<String> tagNames) {
        String sql = "SELECT id FROM tags WHERE name = :name";
        List<Long> tagIds = new ArrayList<>();
        for (String tag : tagNames) {
            MapSqlParameterSource params = new MapSqlParameterSource("name", tag);
            Long tagId = jdbcTemplate.queryForObject(sql, params, Long.class);
            tagIds.add(tagId);
        }
        return tagIds;
    }

    private Integer insertNewLink(String url, List<String> filters) {
        String sql = "INSERT INTO links(url, filters, last_updated_at, type) "
                + "VALUES (:url, :filters, :lastUpdatedAt, :linkType) returning id";
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String filtersStr = String.join(",", filters);
        LinkType linkType = LinkType.getLinkType(url);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("url", url)
                .addValue("filters", filtersStr)
                .addValue("lastUpdatedAt", Timestamp.valueOf(now))
                .addValue("linkType", linkType.toString());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    private void addUserToLink(Integer linkId, Long userId) {
        String sql = "INSERT INTO user_link (link_id, user_id) " + "SELECT :linkId, :userId "
                + "WHERE NOT EXISTS (SELECT 1 FROM user_link WHERE link_id = :linkId AND user_id = :userId)";
        MapSqlParameterSource params =
                new MapSqlParameterSource().addValue("linkId", linkId).addValue("userId", userId);
        jdbcTemplate.update(sql, params);
    }

    private void addTagsToLink(Integer linkId, List<Long> tagIds) {
        String sql = "INSERT INTO link_tag (link_id, tag_id) " + "SELECT :linkId, :tagId "
                + "WHERE NOT EXISTS (SELECT 1 FROM link_tag WHERE link_id = :linkId AND tag_id = :tagId)";
        for (Long tagId : tagIds) {
            MapSqlParameterSource params =
                    new MapSqlParameterSource().addValue("linkId", linkId).addValue("tagId", tagId);
            jdbcTemplate.update(sql, params);
        }
    }
}
