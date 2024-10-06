CREATE TABLE IF NOT EXISTS titles
(
    url        varchar   NOT NULL PRIMARY KEY,
    data       varchar   NOT NULL,
    status     varchar   NOT NULL,
    ttl        timestamp NOT NULL,
    version    varchar   NOT NULL,
    updated_at timestamp NOT NULL DEFAULT now()
);
CREATE INDEX index_name ON titles (url, version);