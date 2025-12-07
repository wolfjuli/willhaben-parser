ALTER TYPE data_type ADD VALUE 'RATING';

COMMIT;
BEGIN;

INSERT INTO attributes (attribute, label, data_type, sorting_attribute)
VALUES ('rating', 'Bewertung', 'RATING', NULL);

INSERT INTO functions (function, name)
VALUES ('0', '0');

INSERT INTO custom_attributes (id, function_id, data_type)
VALUES ((SELECT id FROM attributes WHERE attribute = 'rating'), (SELECT id FROM functions WHERE name = '0'), 'RATING');


/** Materialized views dont allow function calls anymore (for a security reason), thus we have to do it by ourself **/
CREATE OR REPLACE FUNCTION update_normalized_listings(willhaben_ids INTEGER[] DEFAULT NULL::INTEGER[],
                                                      listing_ids INTEGER[] DEFAULT NULL::INTEGER[])
    RETURNS SETOF normalized_listings
    VOLATILE
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY WITH RECURSIVE
        ls AS (
            SELECT id,
                   created_datetime,
                   last_seen,
                   willhaben_id,
                   hash,
                   duplicate_hash,
                   raw || jsonb_build_object('listingId', id) AS raw
            FROM listings
            WHERE (willhaben_ids IS NULL OR willhaben_id = ANY (willhaben_ids))
              AND (listing_ids IS NULL OR id = ANY (listing_ids))
            ),
        ca AS (SELECT l_1.id                                                                     AS listing_id,
                      jsonb_object_agg(a.attribute,
                                       unpack_run_function(f.function, 'null'::jsonb,
                                                           jsonb_build_object('base', l_1.raw))) AS raw
               FROM ls l_1
               CROSS JOIN custom_attributes ca_1
               JOIN attributes a
                   ON a.id = ca_1.id
               JOIN functions f
                   ON f.id = ca_1.function_id
               GROUP BY l_1.id),
        attr_raw AS (SELECT lua.listing_id,
                            a.id AS attribute_id,
                            s.idx,
                            s.part,
                            lua.values
                     FROM listing_user_attributes lua
                     JOIN attributes a
                         ON a.id = lua.attribute_id
                     CROSS JOIN unnest(regexp_split_to_array(a.attribute, '\.')) WITH ORDINALITY s(part, idx)),
        direct_parents AS (SELECT r.listing_id, p.part AS parent, r.part, r.idx, r.values
                           FROM attr_raw r
                           LEFT JOIN attr_raw p
                               ON p.listing_id = r.listing_id
                               AND p.attribute_id = r.attribute_id
                               AND p.idx = r.idx - 1),
        r_ua AS (

            SELECT r.listing_id, r.parent, r.idx, jsonb_object_agg(r.part, r.values) AS obj
            FROM direct_parents r
            WHERE r.part NOT IN (SELECT DISTINCT coalesce(parent, '') FROM direct_parents)
            GROUP BY r.listing_id, r.parent, r.idx

            UNION ALL

            SELECT r.listing_id, r.parent, r.idx, jsonb_object_agg(r.part, r.values) AS obj
            FROM direct_parents r
            JOIN (SELECT x.listing_id, x.parent, max(x.idx) AS idx FROM direct_parents x GROUP BY 1, 2) x
                USING (listing_id, parent, idx)
            GROUP BY r.listing_id, r.parent, r.idx


            UNION ALL

            SELECT r.listing_id, r.parent, r.idx, jsonb_build_object(r.part, r_ua.obj) AS obj
            FROM r_ua
            JOIN direct_parents r
                ON r.listing_id = r_ua.listing_id
                AND r.part = r_ua.parent
                AND r.idx = r_ua.idx - 1),
        ua AS (SELECT DISTINCT listing_id, obj AS raw
               FROM r_ua
               WHERE idx = 1)
        INSERT INTO normalized_listings (listing_id, willhaben_id, listing, md5)
            SELECT l.id                                                                           AS listing_id,
                   l.willhaben_id,
                   jsonb_build_object('base', l.raw, 'custom', ca.raw, 'user', ua.raw)            AS listing,
                   md5(jsonb_build_object('base', l.raw, 'custom', ca.raw, 'user', ua.raw)::TEXT) AS md5
            FROM ls l
            LEFT JOIN ca
                ON l.id = ca.listing_id
            LEFT JOIN ua
                ON l.id = ua.listing_id
            ON CONFLICT (listing_id) DO UPDATE SET willhaben_id = excluded.willhaben_id, listing = excluded.listing, md5 = excluded.md5
            RETURNING listing_id, willhaben_id, listing, md5;

END;
$$;


CREATE OR REPLACE FUNCTION update_listing_points(willhaben_ids INTEGER[] DEFAULT NULL::INTEGER[],
                                                 attribute_ids INT[] DEFAULT NULL::INT[],
                                                 listing_ids INTEGER[] DEFAULT NULL::INTEGER[],
                                                 script_ids INT[] DEFAULT NULL::INT[])

    RETURNS TABLE
            (
                like listing_points
            )
    VOLATILE
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        INSERT INTO listing_points (listing_id, attribute_id, script_id, points)
            SELECT listing_id,
                   attribute_id,
                   s.id,
                   run_script(s.id, listing_id)::DOUBLE PRECISION
            FROM normalized_listings l
            CROSS JOIN scripts s
            WHERE nullif(run_script(s.id, listing_id), 'null') IS NOT NULL
              AND (willhaben_ids IS NULL OR
                   listing_id IN (SELECT id FROM listings WHERE willhaben_id = ANY (willhaben_ids)))
              AND (listing_ids IS NULL OR listing_id = ANY (listing_ids))
              AND (attribute_ids IS NULL OR attribute_id = ANY (attribute_ids))
              AND (script_ids IS NULL OR s.id = ANY (script_ids))
            ON CONFLICT (listing_id, attribute_id, script_id) DO UPDATE SET points = excluded.points
            RETURNING listing_id, attribute_id, script_id, points;
END;
$$;


select count(*)
from update_normalized_listings();
