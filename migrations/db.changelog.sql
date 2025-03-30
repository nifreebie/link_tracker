-- liquibase formatted sql

--changeset nifreebie:001_init_tables
CREATE TABLE Users
(
    id      SERIAL PRIMARY KEY,
    chat_id BIGINT UNIQUE NOT NULL
);

--changeset nifreebie:002_init_tables
CREATE SEQUENCE link_id_seq
    START WITH 1
    INCREMENT BY 1;

-- Создание таблицы links
CREATE TABLE links
(
    id              INTEGER PRIMARY KEY DEFAULT nextval('link_id_seq'),
    url             VARCHAR(255) NOT NULL,
    tags            JSONB,
    filters         JSONB,
    last_updated_at TIMESTAMP    NOT NULL
);

--changeset nifreebie:003_init_tables

CREATE TABLE github_events
(
    id                  SERIAL PRIMARY KEY,
    repository_id       INTEGER      NOT NULL REFERENCES links (id),
    event_type          VARCHAR(20)  NOT NULL,
    title               VARCHAR(255) NOT NULL,
    username            VARCHAR(100) NOT NULL,
    created_at          TIMESTAMP    NOT NULL,
    description_preview VARCHAR(200)
);

--changeset nifreebie:004_init_tables
create table user_link
(
    id      Serial PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users (id),
    link_id Integer NOT NULL REFERENCES links (id)
);

--changeset nifreebie:005_init_tables
ALTER TABLE links
    ALTER COLUMN filters TYPE TEXT;
ALTER TABLE links
    ALTER COLUMN tags TYPE TEXT;


--changeset nifreebie:006_init_tables
ALTER TABLE links
    ALTER COLUMN last_updated_at SET DEFAULT now();

--changeset nifreebie:007_init_tables
ALTER TABLE links
    ADD COLUMN type VARCHAR(255) NOT NULl;

--changeset nifreebie:008_init_tables
CREATE TABLE tags
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

ALTER TABLE links
    DROP COLUMN tags;

create table user_tag
(
    id      Serial PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users (id),
    tag_id  Integer NOT NULL REFERENCES tags (id)
);

create table link_tag
(
    id      Serial PRIMARY KEY,
    link_id INTEGER NOT NULL REFERENCES links (id),
    tag_id  Integer NOT NULL REFERENCES tags (id)
);

--changeset nifreebie:009_init_tables
drop table user_tag;

alter table tags
    add column user_id INTEGER NOT NULL REFERENCES users (id);

