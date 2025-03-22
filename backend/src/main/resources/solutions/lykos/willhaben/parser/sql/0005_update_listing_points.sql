DROP FUNCTION IF EXISTS update_listing_points;

CREATE OR REPLACE FUNCTION update_listing_points(willhaben_ids INT[] = NULL,
                                                 attribute_ids SMALLINT[] = NULL,
                                                 listing_ids INT[] = NULL)
    RETURNS TABLE
            (
                LIKE LISTING_POINTS
            )
    LANGUAGE plpgsql
AS
$$
DECLARE
    ret INTEGER;
BEGIN
    RETURN QUERY
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
            WHERE (willhaben_ids IS NULL OR
                   la.listing_id IN (SELECT id FROM listings WHERE willhaben_id = ANY (willhaben_ids)))
              AND (listing_ids IS NULL OR la.listing_id = ANY (listing_ids))
              AND (attribute_ids IS NULL OR la.attribute_id = ANY (attribute_ids))
            ON CONFLICT (listing_id, attribute_id, script_id) DO UPDATE SET points = excluded.points
            RETURNING listing_id, attribute_id, script_id, points;
END;
$$;


CREATE VIEW fe_update_listing_points AS
SELECT *
FROM update_listing_points();
