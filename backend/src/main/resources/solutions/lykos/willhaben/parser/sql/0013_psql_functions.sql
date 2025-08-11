DROP FUNCTION IF EXISTS run_script(script_id SMALLINT, attribute_id SMALLINT, val TEXT, listing JSONB);
DROP FUNCTION IF EXISTS run_function(_function TEXT, attribute TEXT, listing JSONB);
DROP FUNCTION IF EXISTS run_function(SMALLINT, attribute TEXT, listing JSONB);

DROP FUNCTION update_normalize_listings(willhaben_ids INTEGER[], attribute_ids SMALLINT[], listing_ids INTEGER[]);
DROP TABLE IF EXISTS normalized_listings;
DROP VIEW IF EXISTS normalized_listings;
DROP VIEW IF EXISTS normalized;

DROP TRIGGER IF EXISTS trg_changed_script_functions ON script_functions;
DROP TRIGGER IF EXISTS trg_changed_custom_attributes ON custom_attributes;
DROP TRIGGER IF EXISTS trg_changed_listing_attributes ON listing_attributes;
DROP TRIGGER IF EXISTS trg_changed_user_defined_attributes ON user_defined_attributes;
DROP TRIGGER IF EXISTS trg_changed_listing_custom_attributes ON listing_custom_attributes;
DROP TRIGGER IF EXISTS trg_changed_normalized_listings ON normalized_listings;

ALTER TABLE user_defined_attributes
    ALTER COLUMN values TYPE JSONB USING to_jsonb(values[1]);

ALTER TABLE custom_attributes
    RENAME COLUMN normalized TO attribute;

CREATE OR REPLACE FUNCTION run_function(_function TEXT, attribute TEXT, listing JSONB)
    RETURNS JSONB
    VOLATILE
    LANGUAGE plpgsql
AS
$$
DECLARE
    res JSONB;
    sql TEXT ;
BEGIN
    sql := 'SELECT to_jsonb(' || _function || ') from (values(''' || attribute || ''', ''' || listing::TEXT ||
           '''::JSONB)) r(val, row)';
    EXECUTE sql INTO res;
    RETURN res;
END;
$$;

CREATE OR REPLACE FUNCTION run_function(function_id SMALLINT, attribute TEXT, listing JSONB)
    RETURNS JSONB
    VOLATILE
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN run_function((SELECT function FROM functions WHERE id = function_id), attribute, listing);
END;
$$;



CREATE OR REPLACE FUNCTION run_script_steps(_script_id SMALLINT, _listing_id INTEGER)
    RETURNS TABLE
            (
                SCRIPT_ID  SMALLINT,
                LISTING_ID INTEGER,
                VALUE      JSONB,
                ORD        SMALLINT
            )
    IMMUTABLE
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY WITH RECURSIVE
                     sfs AS (SELECT s.id AS script_id, -s.attribute_id AS function_id, 0 AS ord
                             FROM scripts s

                             UNION ALL

                             SELECT sf.script_id, sf.function_id, sf.ord
                             FROM script_functions sf),
                     fs AS (SELECT -a.id                                         AS function_id,
                                   'jsonb_path_query(row, ''' || a.path || ''')' AS function,
                                   'Get value ' || a.attribute                   AS name,
                                   NULL                                          AS value
                            FROM attributes a

                            UNION ALL

                            SELECT id, function, name, NULL
                            FROM functions),
                     intermediate AS (SELECT sfs.script_id,
                                             run_function(fs.function, '', l.listing) AS value,
                                             l.listing,
                                             sfs.ord
                                      FROM normalized_listings l
                                               JOIN sfs
                                                    ON l.listing_id = _listing_id
                                                        AND sfs.script_id = _script_id
                                                        AND sfs.ord = 0
                                               JOIN fs
                                                    USING (function_id)

                                      UNION ALL

                                      SELECT sfs.script_id,
                                             run_function(fs.function, i.value::TEXT, listing) AS value,
                                             i.listing,
                                             sfs.ord
                                      FROM intermediate i
                                               JOIN sfs
                                                    ON sfs.script_id = i.script_id
                                                        AND sfs.ord = i.ord + 1
                                               JOIN fs
                                                    USING (function_id))
                 SELECT sfs.script_id, _listing_id, value, ord
                 FROM intermediate i;
END;
$$;

CREATE OR REPLACE FUNCTION run_script(_script_id SMALLINT, _listing_id INTEGER)
    RETURNS JSONB
    IMMUTABLE
    LANGUAGE plpgsql
AS
$$
DECLARE
    res JSONB;
BEGIN

    WITH maxs AS (SELECT max(ord) AS ord, sf.script_id
                  FROM script_functions sf
                  WHERE sf.script_id = _script_id
                  GROUP BY sf.script_id)
    SELECT value
    INTO res
    FROM run_script_steps(_script_id, _listing_id) rs
             JOIN maxs m
                  ON rs.script_id = m.script_id
                      AND rs.ord = m.ord;

    RETURN res;

END;
$$;



UPDATE willhaben.functions
SET function = 'val::REAL / 200000',
    name     = '/ 200 000'
WHERE id = 1;
UPDATE willhaben.functions
SET function = 'val::REAL * 2',
    name     = 'Prio 2'
WHERE id = 2;
UPDATE willhaben.functions
SET function = 'val::REAL * 10',
    name     = 'Prio 10'
WHERE id = 3;
UPDATE willhaben.functions
SET function = '''47.067393,15.442110;'' || val::TEXT',
    name     = 'Add Graz Jakominiplatz'
WHERE id = 4;
UPDATE willhaben.functions
SET function = e'st_distance((\'SRID=4326;POINT(\' || replace(split_part(val::TEXT, \';\', 1), \',\', \' \') || \')\')::geography,
                    (\'SRID=4326;POINT(\' || replace(split_part(val::TEXT, \';\', 2), \',\', \' \') || \')\')::geography
        )',
    name     = 'Distance between 2 Points'
WHERE id = 5;
UPDATE willhaben.functions
SET function = 'tanh(val::REAL)',
    name     = 'tanh'
WHERE id = 6;
UPDATE willhaben.functions
SET function = 'pow(0.5772156649, -(val::REAL * val::REAL) / 0.32) / sqrt(2 * PI() * 0.16)',
    name     = 'Normalverteilung (sig=0.4)'
WHERE id = 7;
UPDATE willhaben.functions
SET function = '(val::REAL - 100) / 100',
    name     = 'Wohnfläche Wohnung'
WHERE id = 8;
UPDATE willhaben.functions
SET function = '(400000 - val::REAL) / 400000',
    name     = 'Preis normalisiert'
WHERE id = 9;
UPDATE willhaben.functions
SET function = e'(SELECT exists (
    SELECT FROM unnest(\'{"wohnrecht", "bauherrenmodell", "anlageobjekt", "anlegerwohnung", "anleger", "vermietet", "mieteinnahmen", " bms ", "beteiligungsprojekt", "rohdachboden", "ordinationsräumlichkeiten"}\'::TEXT[]) ws(w)
    WHERE lower(val::TEXT) LIKE \'%\' || ws.w || \'%\')
)::INTEGER * -1',
    name     = 'Schlechte Wörter'
WHERE id = 10;
UPDATE willhaben.functions
SET function = e'(SELECT exists (
    SELECT FROM unnest(\'{"fernblick", "aussicht", "weitblick", "fußbodenheizung", "fussbodenheizung", "garage", "carport", "balkon", "terasse", "terrasse"}\'::TEXT[]) ws(w)
    WHERE lower(val::TEXT) LIKE \'%\' || ws.w || \'%\')
)::INTEGER',
    name     = 'Gute Wörter'
WHERE id = 11;
UPDATE willhaben.functions
SET function = e'jsonb_build_object(\'href\', row->\'base\'->\'attributeMap\'->>\'seoUrl\',
                           \'heading\', coalesce(row->\'base\'->>\'description\',
                                               row->\'base\'->\'attributeMap\'->>\'seoUrl\'
                                      )
        )',
    name     = 'Attr:Link'
WHERE id = 12;
UPDATE willhaben.functions
SET function = e'replace(substring( row->\'base\'->\'attributeMap\'->>\'price\' FROM \'[0-9,.]+\'), \',\', \'.\')::FLOAT /
        greatest(
                coalesce(replace(row->\'base\'->\'attributeMap\'->>\'plotArea\', \',\', \'.\'), \'1\')::FLOAT,
                coalesce(replace(row->\'base\'->\'attributeMap\'->>\'estateSizeLivingArea\', \',\', \'.\'), \'1\')::FLOAT
        )',
    name     = 'Preis/m2'
WHERE id = 13;
UPDATE willhaben.functions
SET function = e'  (jsonb_path_query(row, \'$.base.attributeMap.propertyType\')::TEXT = any(\'{"Loft/Studio","Garconniere","Rohdachboden","Maisonette"}\'))::INT
+ (lower(jsonb_path_query(row, \'$.base.attributeMap.propertyType\')::TEXT) like \'%wohnung%\')::INT
',
    name     = 'ist Wohnung'
WHERE id = 14;
UPDATE willhaben.functions
SET function = e'  (jsonb_path_query(row, \'$.base.attributeMap.propertyType\')::TEXT = any(\'{"Villa","Bungalow","Rohbau"}\'))::INT
+ (lower(jsonb_path_query(row, \'$.base.attributeMap.propertyType\')::TEXT) like \'%haus%\')::INT',
    name     = 'ist Haus'
WHERE id = 15;
UPDATE willhaben.functions
SET function = '(val::FLOAT - 150) / 150',
    name     = 'Wohnfläche Haus'
WHERE id = 16;
UPDATE willhaben.functions
SET function = e'    (coalesce(nullif(split_part(val::TEXT, \'X\', 1), \'\'), \'4\')::REAL< 3.5)::INT * -1 *
       (((row->\'base\'->\'attributeMap\'->>\'estateSizeLivingArea\')::REAL < 80)::INT * 4 + 1)',
    name     = 'Zimmer < 3.5'
WHERE id = 17;
UPDATE willhaben.functions
SET function = '''-''::TEXT',
    name     = 'Attr:Leer'
WHERE id = 18;
UPDATE willhaben.functions
SET function = 'val',
    name     = 'Wohnung Aussenanlagen'
WHERE id = 19;
UPDATE willhaben.functions
SET function = '(8000 - val::REAL) / 8000',
    name     = 'Distanz Normalisiert'
WHERE id = 20;
UPDATE willhaben.functions
SET function = e'(SELECT exists (
        SELECT FROM unnest(\'{"garage", "carport", "parkplatz"}\'::TEXT[]) ws(w)
        WHERE lower(val::TEXT) LIKE \'%\' || ws.w || \'%\')
    )::INTEGER * -1',
    name     = 'Fehlende Wörter'
WHERE id = 22;
UPDATE willhaben.functions
SET function = e'(SELECT exists (
        SELECT sum((lower(val::TEXT) LIKE \'%\' || ws.w || \'%\')::INT)
        FROM unnest(\'{"green tower", "esplanade", "am puls der zeit", "urbangreengeidorf", "falling water", "jakomini verde", "fatestone"}\'::TEXT[]) ws(w)
    )::INTEGER * -1',
    name     = 'No Go'
WHERE id = 23;
UPDATE willhaben.functions
SET function = '(val::REAL > 50) * -1',
    name     = 'Lärm'
WHERE id = 24;
UPDATE willhaben.functions
SET function = '(val::REAL > 700000) * -1',
    name     = 'Preis > 700k'
WHERE id = 25;
UPDATE willhaben.functions
SET function = '(length(val::TEXT) > 0)::INT * -1',
    name     = 'Nicht leer'
WHERE id = 26;
UPDATE willhaben.functions
SET function = e'(SELECT exists (
        SELECT sum((lower(val::TEXT) LIKE \'%\' || ws.w || \'%\')::INT)
        FROM unnest(\'{"graz", "graz-umgebung"}\'::TEXT[]) ws(w)
    )::INTEGER ',
    name     = 'Bezirke Haus'
WHERE id = 27;
UPDATE willhaben.functions
SET function = e'(SELECT exists (
        SELECT sum((lower(val::TEXT) LIKE \'%\' || ws.w || \'%\')::INT)
        FROM unnest(\'{"graz"}\'::TEXT[]) ws(w)
    )::INTEGER ',
    name     = 'Bezirke Wohnung'
WHERE id = 28;
UPDATE willhaben.functions
SET function = '1',
    name     = '1'
WHERE id = 30;


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
                      WHERE attribute = new.attribute),
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

DROP TRIGGER trg_attribute_id ON attributes;
DROP TRIGGER trg_custom_attribute_id ON custom_attributes;

CREATE TRIGGER trg_attribute_id
    BEFORE INSERT
    ON attributes
    FOR EACH ROW
EXECUTE FUNCTION get_id();

CREATE TRIGGER trg_custom_attribute_id
    BEFORE INSERT
    ON custom_attributes
    FOR EACH ROW
EXECUTE FUNCTION get_id();

UPDATE attributes
SET attribute ='attributeMap.' || normalized;

ALTER TABLE attributes
    DROP COLUMN normalized;

ALTER TABLE user_defined_attributes
    ALTER COLUMN values TYPE JSONB USING to_jsonb(values[1]);


DROP FUNCTION IF EXISTS update_listing_custom_attributes;

CREATE OR REPLACE FUNCTION update_listing_custom_attributes(willhaben_ids INT[] = NULL, attribute_ids SMALLINT[] = NULL,
                                                            listing_ids INT[] = NULL, function_ids SMALLINT[] = NULL)
    RETURNS VOID
    VOLATILE
    LANGUAGE plpgsql
AS
$$
BEGIN

    INSERT INTO listing_custom_attributes(listing_id, attribute_id, values)
    SELECT l.id, ca.id, run_function(ca.function_id, '', l.raw)
    FROM listings l
             CROSS JOIN custom_attributes ca
    WHERE (willhaben_ids IS NULL OR l.willhaben_id = ANY (willhaben_ids))
      AND (attribute_ids IS NULL OR ca.id = ANY (attribute_ids))
      AND (listing_ids IS NULL OR l.id = ANY (listing_ids))
      AND (function_ids IS NULL OR ca.function_id = ANY (function_ids));
END;
$$;


CREATE VIEW normalized_listings AS
WITH ca AS (SELECT lca.listing_id, jsonb_object_agg(ca.attribute, lca.values) AS raw
            FROM listing_custom_attributes lca
                     JOIN custom_attributes ca
                          ON lca.attribute_id = ca.id
            GROUP BY 1),
     ua AS (SELECT ua.listing_id, jsonb_object_agg(coalesce(a.attribute, uca.attribute), ua.values) AS raw
            FROM user_defined_attributes ua
                     LEFT JOIN attributes a
                               ON a.id = ua.attribute_id
                     LEFT JOIN custom_attributes uca
                               ON uca.id = ua.attribute_id
            WHERE coalesce(a.attribute, uca.attribute) IS NOT NULL
            GROUP BY 1)
SELECT l.id                           AS listing_id,
       l.willhaben_id                 AS willhaben_id,
       jsonb_build_object(
               'base', l.raw,
               'custom', ca.raw,
               'user', ua.raw)        AS listing,
       md5(jsonb_build_object(
               'base', l.raw,
               'custom', ca.raw,
               'user', ua.raw)::TEXT) AS md5
FROM listings l
         LEFT JOIN listing_custom_attributes lca
                   ON l.id = lca.listing_id
         LEFT JOIN ca
                   ON l.id = ca.listing_id
         LEFT JOIN ua
                   ON l.id = ua.listing_id
;


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
            SELECT listing_id,
                   attribute_id,
                   s.id,
                   run_script(s.id, listing_id)
            FROM normalized_listings l
                     CROSS JOIN scripts s
            WHERE (willhaben_ids IS NULL OR
                   listing_id IN (SELECT id FROM listings WHERE willhaben_id = ANY (willhaben_ids)))
              AND (listing_ids IS NULL OR listing_id = ANY (listing_ids))
              AND (attribute_ids IS NULL OR attribute_id = ANY (attribute_ids))
              AND (script_ids IS NULL OR s.id = ANY (script_ids))
            ON CONFLICT (listing_id, attribute_id, script_id) DO UPDATE SET points = excluded.points
            RETURNING listing_id, attribute_id, script_id, points;
END;
$$;


CREATE OR REPLACE FUNCTION listing_path_query(listing JSONB, path TEXT) RETURNS JSONB
    IMMUTABLE
    LANGUAGE plpgsql
AS
$$
DECLARE
    ret JSONB;
BEGIN
    SELECT coalesce(u.val, c.val, b.val)
    INTO ret
    FROM jsonb_path_query(listing, ('$.user.' || path)::JSONPATH) WITH ORDINALITY u(val, idx)
             FULL JOIN jsonb_path_query(listing, ('$.custom.' || path)::JSONPATH) WITH ORDINALITY c(val, idx)
                       USING (idx)
             FULL JOIN jsonb_path_query(listing, ('$.base.' || path)::JSONPATH) WITH ORDINALITY b(val, idx)
                       USING (idx)
    LIMIT 1;

    RETURN ret;
END
$$;
