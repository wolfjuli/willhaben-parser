-- Disable triggers for now
SELECT toggle_triggers('disable');


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
        WITH lids AS MATERIALIZED (SELECT l.id, l.willhaben_id
                                   FROM listings l
                                   WHERE (willhaben_ids IS NULL OR l.willhaben_id = ANY (willhaben_ids))
                                     AND (listing_ids IS NULL OR l.id = ANY (listing_ids))
                                     AND (attribute_ids IS NULL OR
                                          exists (SELECT FROM custom_attributes WHERE id = ANY (attribute_ids)) OR
                                          exists (SELECT
                                                  FROM listing_attributes la
                                                  WHERE attribute_id = ANY (attribute_ids)
                                                    AND listing_id = l.id))
                                   ORDER BY 1),
            cas AS (SELECT lca.listing_id, lca.attribute_id, lca.values
                    FROM listing_custom_attributes lca
                    CROSS JOIN update_listing_custom_attributes(listing_ids := (SELECT coalesce(array_agg(l.id), ARRAY []::INT[])
                                                                                FROM lids l
                                                                                LEFT JOIN listing_custom_attributes lca
                                                                                    ON lca.listing_id = l.id
                                                                                WHERE lca.listing_id IS NULL)
                               )
                    WHERE lca.listing_id IN (SELECT id FROM lids))
            ,
            attrs AS MATERIALIZED (SELECT a.id, a.normalized
                                   FROM attributes a
                                   UNION ALL
                                   SELECT caa.id, caa.normalized
                                   FROM custom_attributes caa),
            new_listings AS (
                SELECT l.id                                              AS listing_id,

                       l.willhaben_id,
                       jsonb_object_agg(a.normalized,
                                        jsonb_build_object(
                                                'base', array_to_string(
                                                (SELECT values
                                                 FROM listing_attributes
                                                 WHERE listing_id = l.id
                                                   AND attribute_id = a.id),
                                                ','),
                                                'custom',
                                                (SELECT values FROM cas WHERE listing_id = l.id AND attribute_id = a.id),
                                                'user', array_to_string((SELECT values
                                                                         FROM user_defined_attributes
                                                                         WHERE listing_id = l.id
                                                                           AND attribute_id = a.id), ',')
                                        )) ||
                       jsonb_build_object('id', l.id) ||
                       jsonb_build_object('willhabenId', l.willhaben_id) AS listing
                FROM lids l
                CROSS JOIN attrs a
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

CREATE INDEX ON listings (willhaben_id);

SELECT count(*)
FROM update_normalize_listings(listing_ids := (SELECT array_agg(id)
                                               FROM listings
                                               WHERE id NOT IN (SELECT listing_id FROM listing_custom_attributes)
                                                  OR created_datetime::DATE >= now()::DATE - '3 days'::INTERVAL)
     );


CREATE OR REPLACE FUNCTION changed_user_defined_attributes()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    PERFORM update_normalize_listings(listing_ids := ARRAY [coalesce(new.listing_id, old.listing_id)]);
    PERFORM update_listing_points(listing_ids := ARRAY [coalesce(new.listing_id, old.listing_id)]);
    RETURN coalesce(new, old);
END;
$$;


SELECT toggle_triggers('enable');
