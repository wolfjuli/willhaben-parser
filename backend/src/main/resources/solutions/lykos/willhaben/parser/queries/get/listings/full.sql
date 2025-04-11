-- listings/full
SELECT listing, md5
FROM normalized_listings nl
JOIN listings l
    ON nl.listing_id = l.id
WHERE (${knownMd5}::TEXT[] IS NULL OR NOT md5 = ANY (${knownMd5}::TEXT[]))
  AND (${listingId}::INT IS NULL OR ${listingId}::INT = nl.listing_id)
  AND last_seen = (SELECT max(last_seen) FROM listings)
;
