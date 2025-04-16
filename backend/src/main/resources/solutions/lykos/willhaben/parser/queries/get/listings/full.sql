-- listings/full
SELECT listing || jsonb_build_object('points', sum(coalesce(lp.points, 0))) as listing, md5
FROM normalized_listings nl
JOIN listings l
    ON nl.listing_id = l.id
LEFT JOIN listing_points lp
    ON l.id = lp.listing_id
WHERE (${knownMd5}::TEXT[] IS NULL OR NOT md5 = ANY (${knownMd5}::TEXT[]))
  AND (${ids}::INT[] IS NULL OR nl.listing_id = ANY (${ids}::INT[]))
  AND last_seen = (SELECT max(last_seen) FROM listings)
GROUP BY listing, md5
;
