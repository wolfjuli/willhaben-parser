--setUserListingAttribute
WITH ins AS (
    INSERT INTO listing_user_attributes (listing_id, attribute_id, values)
        SELECT ${listingId}::INT, ${attributeId}::INT, to_jsonb(nullif(${values}::TEXT, 'null'))
        WHERE to_jsonb(nullif(${values}::TEXT, 'null')) IS NOT NULL
        ON CONFLICT (listing_id, attribute_id) DO UPDATE
            SET values = excluded.values
        RETURNING listing_id)
SELECT listing_id, willhaben_id, listing, md5
FROM update_normalized_listings(listing_ids := (SELECT array_agg(listing_id)
                                                FROM (SELECT ${listingId}::INT as listing_id UNION SELECT listing_id FROM ins) x))
;
