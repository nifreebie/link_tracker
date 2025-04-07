CREATE TABLE users
(
    id      SERIAL PRIMARY KEY,
    chat_id BIGINT UNIQUE NOT NULL
);

CREATE TABLE links
(
    id              SERIAL PRIMARY KEY,
    url             VARCHAR(255) NOT NULL,
    filters         TEXT,
    last_updated_at TIMESTAMP    NOT NULL DEFAULT now(),
    type            VARCHAR(255) NOT NULL
);

CREATE TABLE user_link
(
    id      SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users (id),
    link_id INTEGER NOT NULL REFERENCES links (id)
);

CREATE TABLE tags
(
    id      SERIAL PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    user_id INTEGER      NOT NULL REFERENCES users (id)
);

CREATE TABLE link_tag
(
    id      SERIAL PRIMARY KEY,
    link_id INTEGER NOT NULL REFERENCES links (id),
    tag_id  INTEGER NOT NULL REFERENCES tags (id)
);

CREATE INDEX users_chat_id_hash_idx ON users USING hash (chat_id);

CREATE INDEX links_url_hash_idx ON links USING hash (url);

CREATE INDEX tags_name_hash_idx ON tags USING hash (name);
