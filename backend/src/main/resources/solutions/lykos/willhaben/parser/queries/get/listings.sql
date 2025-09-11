-- listings
SELECT listing ||
       jsonb_build_object('custom', listing -> 'custom' || jsonb_build_object('points', sum(coalesce(lp.points, 0)))) ||
       jsonb_build_object('id', l.listing_id) AS listing,
       md5
FROM normalized_listings l
         LEFT JOIN listing_points lp
                   ON l.listing_id = lp.listing_id
WHERE (${knownMd5}::TEXT[] IS NULL OR NOT md5 = ANY (${knownMd5}::TEXT[]))
  AND (${ids}::INT[] IS NULL OR l.listing_id = ANY (${ids}::INT[]))
--AND l.last_seen = (SELECT max(last_seen) FROM listings)
GROUP BY listing, md5, l.listing_id, l.willhaben_id
;
