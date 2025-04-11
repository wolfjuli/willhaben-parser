-- listings/sorting
SELECT nl.listing_id, nl.listing -> 'points' -> 'base' AS points
FROM (SELECT listing_id,
             listing || jsonb_build_object('points', json_build_object('base', sum(coalesce(lp.points, 0)))) AS listing
      FROM normalized_listings
      LEFT JOIN listing_points lp
          USING (listing_id)
      GROUP BY listing_id, listing) nl
JOIN listings l
    ON nl.listing_id = l.id
WHERE l.last_seen = (SELECT max(last_seen) FROM listings)
ORDER BY COALESCE(nl.listing->${sortCol}->'user', COALESCE (nl.listing -> ${sortCol} -> 'custom', nl.listing ->
             ${sortCol} -> 'base')) ${sortDir}
