CREATE TABLE custom_attributes
(
    id          SMALLINT  NOT NULL PRIMARY KEY REFERENCES attribute_mapping (id) ON DELETE CASCADE ON UPDATE CASCADE,
    normalized  TEXT      NOT NULL,
    label       TEXT      NOT NULL,
    function_id SMALLINT  NOT NULL REFERENCES functions (id) ON DELETE CASCADE ON UPDATE CASCADE,
    data_type   DATA_TYPE NOT NULL,
    UNIQUE (normalized)
);

CREATE TRIGGER trg_custom_attribute_id
    BEFORE INSERT OR UPDATE
    ON custom_attributes
    FOR EACH ROW
EXECUTE FUNCTION get_id();


INSERT INTO functions (function, name)
VALUES ('(val, row) => ({href: row["seoUrl"], value: row["heading"]})', 'Link'),
       ('(val, row) => +row["price"] / +row["estateSize"]', 'Preis/m2');

INSERT INTO custom_attributes (normalized, label, function_id, data_type)
VALUES ('seoUrl', 'Link', (SELECT id FROM functions WHERE name = 'Link'), 'LINK'),
       ('pricePerArea', 'Preis/m2', (SELECT id FROM functions WHERE name = 'Preis/m2'), 'TEXT');

CREATE OR REPLACE VIEW all_attributes AS
SELECT id, normalized, label, function_id, data_type
FROM custom_attributes
UNION ALL
SELECT id, normalized, label, NULL, data_type
FROM attributes;

CREATE OR REPLACE VIEW normalized_listings AS
SELECT la.listing_id, jsonb_object_agg(a.normalized, array_to_string(la.values, ',')) AS listing
FROM listing_attributes la
         JOIN all_attributes a
              ON la.attribute_id = a.id
GROUP BY la.listing_id;


CREATE OR REPLACE VIEW fe_scripts AS
SELECT s.id,
       a.id                                                                                  AS attribute_id,
       coalesce(s.name, 'script' || s.id)                                                    AS name,
       jsonb_agg(
               jsonb_build_object('functionId', sf.function_id, 'ord', sf.ord) ORDER BY ord) AS functions
FROM scripts s
         JOIN all_attributes a
              ON s.attribute_id = a.id
         LEFT JOIN script_functions sf
                   ON s.id = sf.script_id
GROUP BY s.id, a.id, s.name, s.id
;
