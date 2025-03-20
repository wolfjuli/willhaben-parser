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

DROP VIEW fe_listings;

CREATE OR REPLACE VIEW fe_listings AS
SELECT l.id,
       l.willhaben_id,
       coalesce(lp.points, 0)                                         AS points,
       jsonb_object_agg(a.normalized, coalesce(ua.values, la.values)) AS attributes
FROM listings l
         JOIN listing_attributes la
              ON l.id = la.listing_id
         JOIN all_attributes a
              ON la.attribute_id = a.id
         LEFT JOIN (SELECT listing_id, sum(points) AS points
                    FROM listing_points
                    GROUP BY listing_id) lp
                   ON l.id = lp.listing_id
         LEFT JOIN user_defined_attributes ua
                   ON la.listing_id = ua.listing_id
                       AND la.attribute_id = ua.listing_id
WHERE l.last_seen = (SELECT max(last_seen) FROM listings)
GROUP BY 1, 2, 3
;

DROP VIEW IF EXISTS fe_user_listings;
CREATE OR REPLACE VIEW fe_user_listings AS
SELECT l.id                                      AS listing_id,
       l.willhaben_id,
       jsonb_object_agg(a.normalized, la.values) AS attributes
FROM listings l
         JOIN user_defined_attributes la
              ON l.id = la.listing_id
         JOIN all_attributes a
              ON la.attribute_id = a.id
WHERE l.last_seen = (SELECT max(last_seen) FROM listings)
GROUP BY 1, 2
;

DROP FUNCTION IF EXISTS update_listing_points;

CREATE OR REPLACE FUNCTION update_listing_points(willhaben_ids INT[] = NULL,
                                                 attribute_ids SMALLINT[] = NULL,
                                                 listing_ids INT[] = NULL)
    RETURNS INTEGER
    LANGUAGE plpgsql
AS
$$
DECLARE
    ret INTEGER;
BEGIN
    WITH ins AS (
        INSERT INTO listing_points (listing_id, attribute_id, script_id, points)
    SELECT la.listing_id,
           la.attribute_id,
           s.id,
           run_script(s.id, la.attribute_id::SMALLINT, coalesce(ua.values[1], la.values[1]), nl.listing)
    FROM listing_attributes la
             JOIN normalized_listings nl
                  ON nl.listing_id = la.listing_id
             JOIN scripts s
                  ON s.attribute_id = la.attribute_id
             LEFT JOIN user_defined_attributes ua
                       ON ua.listing_id = la.listing_id
                           AND ua.attribute_id = la.attribute_id
    WHERE (willhaben_ids IS NULL OR la.listing_id IN (SELECT id FROM listings WHERE willhaben_id = ANY (willhaben_ids)))
      AND (listing_ids IS NULL OR la.listing_id = ANY (listing_ids))
      AND (attribute_ids IS NULL OR la.attribute_id = ANY (attribute_ids))
            ON CONFLICT (listing_id, attribute_id, script_id) DO UPDATE SET points = excluded.points
            RETURNING listing_id)
    SELECT count(*)
    INTO ret
    FROM ins;

    RETURN ret;
END;
$$;

SELECT update_listing_points('{1850951858}'::INT[])
