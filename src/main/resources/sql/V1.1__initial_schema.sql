CREATE SEQUENCE preferences_metadata_id_seq;

CREATE TABLE preferences_metadata (
    id INTEGER NOT NULL DEFAULT nextval('preferences_metadata_id_seq'),
    slug VARCHAR PRIMARY KEY,
    description VARCHAR NOT NULL,
    schema VARCHAR NOT NULL
);

CREATE TABLE preferences (
    id VARCHAR NOT NULL,
    preferences_metadata_id INTEGER NOT NULL REFERENCES preferences_metadata(id),
    payload VARCHAR NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE preferences add constraint compound_pk primary key(
    id, preferences_metadata_id
);
