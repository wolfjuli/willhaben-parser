CREATE TABLE schema_version
(
    id               smallint PRIMARY KEY,
    name             TEXT,
    created_datetime timestamptz with DEFAULT current_timestamp
);


