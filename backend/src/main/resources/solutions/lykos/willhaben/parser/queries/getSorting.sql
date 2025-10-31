-- getSorting
SELECT nl.listing_id
FROM (SELECT listing_id,
             listing || jsonb_build_object('custom', listing -> 'custom' ||
                                                     jsonb_build_object('points', coalesce(sum(lp.points), 0))) AS listing
      FROM normalized_listings
      LEFT JOIN listing_points lp
          USING (listing_id)
      GROUP BY listing_id, listing) nl
JOIN listings l
    ON nl.listing_id = l.id
WHERE
    -- l.last_seen = (SELECT max(last_seen) FROM listings) AND
    (${searchString}::TEXT IS NULL
        OR willhaben_id::TEXT LIKE '%' || ${searchString}::TEXT || '%'
        OR exists (SELECT
                   FROM unnest(${searchAttributes}::TEXT[]) a(attribute)
                   WHERE lower(get_listing_attribute(nl.listing, a.attribute)::TEXT) LIKE
                         '%' || lower(${searchString}::TEXT) || '%'))
ORDER BY l.last_seen DESC,
         get_listing_attribute(nl.listing, ${sortCol}) ${sortDir} NULLS LAST
