WITH ins AS
         (
             INSERT INTO listings (willhaben_id, hash, duplicate_hash, raw)
                 VALUES ((SELECT coalesce(min(willhaben_id), 0) - 1 FROM listings WHERE willhaben_id < 0),
                         md5(now()::TEXT),
                         '',
                         jsonb_set(${listing}::TEXT::jsonb -> 'base', '{id}'::TEXT[],
                                   to_jsonb((SELECT coalesce(min(willhaben_id), 0) - 1
                                             FROM listings
                                             WHERE willhaben_id < 0))))
                 RETURNING id)
SELECT listing ||
       jsonb_build_object('points', sum(coalesce(lp.points, 0))) ||
       jsonb_build_object('id', l.listing_id) AS listing,
       md5
FROM update_normalized_listings(listing_ids := (SELECT array_agg(id) FROM ins)) l
LEFT JOIN update_listing_points(listing_ids := (SELECT array_agg(id) FROM ins)) lp
    ON l.listing_id = lp.listing_id
GROUP BY listing, md5, l.listing_id, l.willhaben_id

