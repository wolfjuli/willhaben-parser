CREATE INDEX ON attributes (normalized);
CREATE INDEX ON custom_attributes (normalized);
CREATE INDEX ON listing_attributes (attribute_id);

ANALYSE attributes;
ANALYSE custom_attributes;
ANALYSE listing_attributes;

DROP VIEW IF EXISTS normalized_listings CASCADE;
DROP VIEW IF EXISTS fe_scripts CASCADE;
DROP VIEW IF EXISTS fe_user_listings CASCADE;
DROP VIEW IF EXISTS fe_listings CASCADE;
DROP VIEW IF EXISTS all_attributes CASCADE;


ALTER TABLE attributes
    DROP normalized,
    ADD normalized TEXT GENERATED ALWAYS AS (
        lower(left(replace(initcap(replace(lower(replace(attribute, '/', ' ')), '_', ' ')), ' ', ''), 1)) ||
        right(replace(initcap(replace(lower(replace(attribute, '/', ' ')), '_', ' ')), ' ', ''), -1)) STORED;

DROP TABLE IF EXISTS normalized_listings CASCADE;


CREATE TABLE normalized_listings
(
    listing_id INT NOT NULL PRIMARY KEY REFERENCES listings (id) ON DELETE CASCADE ON UPDATE CASCADE,
    willhaben_id INT      NOT NULL,
    listing      JSONB    NOT NULL,
    md5          TEXT     NOT NULL
);

INSERT INTO normalized_listings (listing_id, willhaben_id, listing, md5)
SELECT la.listing_id,
       l.willhaben_id,
       jsonb_object_agg(a.normalized, array_to_string(la.values, ',')) || jsonb_build_object('id', la.listing_id) ||
       jsonb_build_object('willhabenId', l.willhaben_id),
       md5(jsonb_object_agg(a.normalized, array_to_string(la.values, ','))::TEXT)
FROM listing_attributes la
JOIN (SELECT id, normalized FROM attributes UNION ALL SELECT id, normalized FROM custom_attributes) a
    ON a.id = la.attribute_id
JOIN listings l
    ON l.id = la.listing_id
GROUP BY la.listing_id, l.willhaben_id;

CREATE INDEX ON normalized_listings (md5);
CREATE INDEX ON normalized_listings (willhaben_id);
CREATE INDEX ON normalized_listings USING gin (listing);
ANALYSE normalized_listings;

DROP TABLE IF EXISTS listing_custom_attributes CASCADE;

CREATE TABLE listing_custom_attributes
(
    listing_id   INTEGER REFERENCES listings (id) ON DELETE CASCADE ON UPDATE CASCADE,
    attribute_id SMALLINT REFERENCES attribute_mapping (id) ON DELETE CASCADE ON UPDATE CASCADE,
    values       JSONB,
    PRIMARY KEY (listing_id, attribute_id)
);

ROLLBACK;
DROP FUNCTION IF EXISTS update_listing_custom_attributes(willhaben_ids INT[], attribute_ids SMALLINT[], listing_ids INT[]);
DROP FUNCTION IF EXISTS update_listing_custom_attributes(willhaben_ids INT[], attribute_ids SMALLINT[],
                                                         listing_ids INT[], function_ids SMALLINT[]);

CREATE OR REPLACE FUNCTION update_listing_custom_attributes(willhaben_ids INT[] = NULL, attribute_ids SMALLINT[] = NULL,
                                                            listing_ids INT[] = NULL, function_ids SMALLINT[] = NULL)
    RETURNS TABLE
            (
                LIKE LISTING_CUSTOM_ATTRIBUTES
            )
    LANGUAGE plpgsql
AS
$$
BEGIN

    RETURN QUERY
        INSERT INTO listing_custom_attributes (listing_id, attribute_id, values)
            SELECT l.listing_id, ca.id, run_function(ca.function_id, '', l.listing)
            FROM normalized_listings l
            CROSS JOIN custom_attributes ca
            WHERE (willhaben_ids IS NULL OR
                   listing_id IN (SELECT id FROM listings WHERE willhaben_id = ANY (willhaben_ids)))
              AND (listing_ids IS NULL OR listing_id = ANY (listing_ids))
              AND (attribute_ids IS NULL OR ca.id = ANY (attribute_ids))
              AND (function_ids IS NULL OR ca.function_id = ANY (function_ids))
            ON CONFLICT (listing_id, attribute_id) DO UPDATE SET values = excluded.values
            RETURNING listing_id, attribute_id, values;
END;
$$;


DROP FUNCTION IF EXISTS update_listing_points(willhaben_ids INT[], attribute_ids SMALLINT[], listing_ids INT[],
                                              script_ids SMALLINT[]) CASCADE;
DROP FUNCTION IF EXISTS update_listing_points(willhaben_ids INTEGER[], attribute_ids SMALLINT[],
                                              listing_ids INTEGER[]) CASCADE;

CREATE OR REPLACE FUNCTION update_listing_points(willhaben_ids INT[] = NULL,
                                                 attribute_ids SMALLINT[] = NULL,
                                                 listing_ids INT[] = NULL,
                                                 script_ids SMALLINT[] = NULL)
    RETURNS TABLE
            (
                LIKE LISTING_POINTS
            )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        INSERT INTO listing_points (listing_id, attribute_id, script_id, points)
            WITH uds AS (SELECT ua.listing_id, ua.attribute_id, ua.values, s.id AS script_id
                         FROM user_defined_attributes ua
                         JOIN scripts s
                             ON s.attribute_id = ua.attribute_id),
                 cas AS (SELECT l.listing_id,
                                ca.id                                                     AS attribute_id,
                                ARRAY [run_function(ca.function_id, '', l.listing)::TEXT] AS values,
                                s.id                                                      AS script_id
                         FROM custom_attributes ca
                         CROSS JOIN normalized_listings l
                         JOIN scripts s
                             ON s.attribute_id = ca.id),
                 las AS (SELECT ua.listing_id, ua.attribute_id, ua.values, s.id AS script_id
                         FROM listing_attributes ua
                         JOIN scripts s
                             ON s.attribute_id = ua.attribute_id)
            SELECT listing_id,
                   attribute_id,
                   script_id,
                   run_script(script_id, attribute_id::SMALLINT,
                              array_to_string(coalesce(uds.values, cas.values, las.values), ','), '{}'::JSONB)
            FROM uds
            FULL JOIN cas
                USING (listing_id, attribute_id, script_id)
            FULL JOIN las
                USING (listing_id, attribute_id, script_id)
            WHERE (willhaben_ids IS NULL OR
                   listing_id IN (SELECT id FROM listings WHERE willhaben_id = ANY (willhaben_ids)))
              AND (listing_ids IS NULL OR listing_id = ANY (listing_ids))
              AND (attribute_ids IS NULL OR attribute_id = ANY (attribute_ids))
              AND (script_ids IS NULL OR script_id = ANY (script_ids))
            ON CONFLICT (listing_id, attribute_id, script_id) DO UPDATE SET points = excluded.points
            RETURNING listing_id, attribute_id, script_id, points;
END;
$$;

DROP FUNCTION IF EXISTS update_normalize_listings;
CREATE OR REPLACE FUNCTION update_normalize_listings(willhaben_ids INT[] = NULL,
                                                     attribute_ids SMALLINT[] = NULL,
                                                     listing_ids INT[] = NULL)
    RETURNS TABLE
            (
                LIKE NORMALIZED_LISTINGS
            )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        WITH new_listings AS (
            SELECT l.id                                              AS listing_id,
                   l.willhaben_id,
                   jsonb_object_agg(coalesce(caa.normalized, a.normalized), jsonb_build_object(
                           'base', array_to_string(la.values, ','),
                           'custom', ca.values,
                           'user', array_to_string(ua.values, ',')
                                                                            )) ||
                   jsonb_build_object('id', l.id) ||
                   jsonb_build_object('willhabenId', l.willhaben_id) AS listing
            FROM listings l
            CROSS JOIN attribute_mapping am
            LEFT JOIN listing_attributes la
                ON l.id = la.listing_id
                AND am.id = la.attribute_id
            LEFT JOIN listing_custom_attributes ca
                ON l.id = ca.listing_id
                AND am.id = ca.attribute_id
            LEFT JOIN custom_attributes caa
                ON caa.id = ca.attribute_id
            LEFT JOIN user_defined_attributes ua
                ON am.id = ua.attribute_id
                AND l.id = ua.listing_id
            LEFT JOIN attributes a
                ON a.id = am.id
            WHERE coalesce(ua.attribute_id, caa.id, la.attribute_id) IS NOT NULL
              AND (willhaben_ids IS NULL OR l.willhaben_id = ANY (willhaben_ids))
              AND (listing_ids IS NULL OR l.id = ANY (listing_ids))
              AND (attribute_ids IS NULL OR am.id = ANY (attribute_ids))
            GROUP BY l.id, l.willhaben_id
            )
            INSERT INTO normalized_listings (listing_id, willhaben_id, listing, md5)
                SELECT listing_id, willhaben_id, listing, md5(listing::TEXT)
                FROM new_listings
                ON CONFLICT (listing_id) DO UPDATE
                    SET willhaben_id = excluded.willhaben_id,
                        listing = excluded.listing,
                        md5 = excluded.md5
                RETURNING listing_id, willhaben_id, listing, md5;
END;
$$;

SELECT *
FROM update_normalize_listings();

CREATE OR REPLACE FUNCTION get_id()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
DECLARE
    new_id SMALLINT;
BEGIN
    WITH existing AS (SELECT id
                      FROM attributes
                      WHERE normalized = new.normalized),
         ins AS (
             INSERT INTO attribute_mapping SELECT
                                           WHERE NOT exists (SELECT FROM existing)
                 RETURNING id)
    SELECT id
    INTO new_id
    FROM (SELECT id FROM existing UNION ALL SELECT id FROM ins) x;

    new.id := new_id;
    RETURN coalesce(new, old);
END;
$$;

-- UPDATE NORMALIZED LISTINGS
DROP FUNCTION IF EXISTS ins_upd_listing_attributes CASCADE;
DROP FUNCTION IF EXISTS changed_listing_attributes CASCADE;
CREATE OR REPLACE FUNCTION changed_listing_attributes()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    PERFORM update_listing_custom_attributes(listing_ids := ARRAY [coalesce(new.listing_id, old.listing_id)]);
    RETURN coalesce(new, old);
END;
$$;
DROP TRIGGER IF EXISTS trg_ins_upd_listing_attributes ON listing_attributes;
DROP TRIGGER IF EXISTS trg_ins_upd_del_listing_attributes ON listing_attributes;
CREATE TRIGGER trg_changed_listing_attributes
    AFTER INSERT OR UPDATE OR DELETE
    ON listing_attributes
    FOR EACH ROW
EXECUTE FUNCTION changed_listing_attributes();

DROP FUNCTION IF EXISTS ins_upd_listing_custom_attributes CASCADE;
CREATE OR REPLACE FUNCTION change_listing_custom_attributes()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    PERFORM update_normalize_listings(listing_ids := ARRAY [coalesce(new.listing_id, old.listing_id)]);
    RETURN coalesce(new, old);
END;
$$;
DROP TRIGGER IF EXISTS trg_ins_upd_listing_custom_attributes ON listing_custom_attributes;
DROP TRIGGER IF EXISTS trg_changed_listing_custom_attributes ON listing_custom_attributes;
CREATE TRIGGER trg_changed_listing_custom_attributes
    AFTER INSERT OR UPDATE OR DELETE
    ON listing_custom_attributes
    FOR EACH ROW
EXECUTE FUNCTION change_listing_custom_attributes();

-- UPDATE LISTING POINTS

DROP FUNCTION IF EXISTS ins_upd_normalized_listings CASCADE;
DROP FUNCTION IF EXISTS changed_normalized_listings CASCADE;
CREATE OR REPLACE FUNCTION changed_normalized_listings()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    PERFORM update_listing_points(listing_ids := ARRAY [coalesce(new.listing_id, old.listing_id)]);
    RETURN coalesce(new, old);
END;
$$;
DROP TRIGGER IF EXISTS trg_ins_upd_normalized_listings ON normalized_listings;
CREATE TRIGGER trg_changed_normalized_listings
    AFTER INSERT OR UPDATE OR DELETE
    ON normalized_listings
    FOR EACH ROW
EXECUTE FUNCTION changed_normalized_listings();

DROP FUNCTION IF EXISTS ins_upd_user_defined_attributes CASCADE;
DROP FUNCTION IF EXISTS changed_user_defined_attributes CASCADE;
CREATE OR REPLACE FUNCTION changed_user_defined_attributes()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    PERFORM update_normalize_listings(listing_ids := ARRAY [coalesce(new.listing_id, old.listing_id)]);
    RETURN coalesce(new, old);
END;
$$;
DROP TRIGGER IF EXISTS trg_ins_upd_user_defined_attributes ON user_defined_attributes;
DROP TRIGGER IF EXISTS trg_ins_upd_del_user_defined_attributes ON user_defined_attributes;
CREATE TRIGGER trg_changed_user_defined_attributes
    AFTER INSERT OR UPDATE OR DELETE
    ON user_defined_attributes
    FOR EACH ROW
EXECUTE FUNCTION changed_user_defined_attributes();

DROP FUNCTION IF EXISTS ins_upd_script_functions CASCADE;
DROP FUNCTION IF EXISTS changed_script_functions CASCADE;
CREATE OR REPLACE FUNCTION changed_script_functions()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    PERFORM update_listing_points(script_ids := ARRAY [coalesce(new.script_id, old.script_id)]);
    RETURN coalesce(new, old);
END;
$$;
DROP TRIGGER IF EXISTS trg_ins_upd_script_functions ON script_functions;
DROP TRIGGER IF EXISTS trg_ins_upd_del_script_functions ON script_functions;
CREATE TRIGGER trg_changed_script_functions
    AFTER INSERT OR UPDATE OR DELETE
    ON script_functions
    FOR EACH ROW
EXECUTE FUNCTION changed_script_functions();

DROP FUNCTION IF EXISTS ins_upd_custom_attributes CASCADE;
DROP FUNCTION IF EXISTS changed_custom_attributes CASCADE;
CREATE OR REPLACE FUNCTION changed_custom_attributes()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    PERFORM update_listing_custom_attributes(attribute_ids := ARRAY [coalesce(new.id, old.id)]);
    RETURN coalesce(new, old);
END;
$$;
DROP TRIGGER IF EXISTS trg_ins_upd_custom_attributes ON custom_attributes;
DROP TRIGGER IF EXISTS trg_ins_upd_del_custom_attributes ON custom_attributes;
CREATE TRIGGER trg_changed_custom_attributes
    AFTER INSERT OR UPDATE OR DELETE
    ON custom_attributes
    FOR EACH ROW
EXECUTE FUNCTION changed_custom_attributes();


DROP FUNCTION IF EXISTS ins_upd_functions CASCADE;
DROP FUNCTION IF EXISTS changed_functions CASCADE;
CREATE OR REPLACE FUNCTION changed_functions()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    PERFORM update_listing_custom_attributes(function_ids := ARRAY [coalesce(new.id, old.id)]);
    RETURN coalesce(new, old);
END;
$$;
DROP TRIGGER IF EXISTS trg_ins_upd_functions ON functions;
DROP TRIGGER IF EXISTS trg_ins_upd_del_functions ON functions;
CREATE TRIGGER trg_changed_functions
    AFTER INSERT OR UPDATE OR DELETE
    ON functions
    FOR EACH ROW
EXECUTE FUNCTION changed_functions();

-- DATA
UPDATE functions
SET function = '(val, row) => (row["seoUrl"]?.base && row["heading"]?.base) ? {href: row["seoUrl"]?.base, value: row["heading"].base } : undefined'
WHERE name = 'Attr:Link'
