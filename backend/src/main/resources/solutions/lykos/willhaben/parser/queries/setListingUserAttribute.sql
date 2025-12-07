--setUserListingAttribute
WITH ins AS (
    INSERT INTO listing_user_attributes (listing_id, attribute_id, values)
        SELECT ${listingId}::INT, ${attributeId}::INT, to_jsonb(nullif(${values}::TEXT, 'null'))
        WHERE to_jsonb(nullif(${values}::TEXT, 'null')) IS NOT NULL
        ON CONFLICT (listing_id, attribute_id) DO UPDATE
            SET values = excluded.values
        RETURNING listing_id)
SELECT listing ||
       jsonb_build_object('points', sum(coalesce(lp.points, 0))) ||
       jsonb_build_object('id', l.listing_id) AS listing,
       md5
FROM update_normalized_listings(listing_ids := (SELECT array_agg(listing_id)
                                                FROM (SELECT ${listingId}::INT AS listing_id
                                                      UNION
                                                      SELECT listing_id
                                                      FROM ins) x)) l
JOIN update_listing_points(listing_ids := (SELECT array_agg(listing_id)
                                           FROM (SELECT ${listingId}::INT AS listing_id
                                                 UNION
                                                 SELECT listing_id
                                                 FROM ins) x)) lp
    ON lp.listing_id = l.listing_id
GROUP BY listing, l.listing_id, md5
;
