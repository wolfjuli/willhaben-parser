-- listings/search
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
    (array_length(${searchString}::TEXT[], 1) IS NULL
        OR exists (SELECT
                   FROM unnest(${searchAttrs}::TEXT[]) a(attribute)
                   CROSS JOIN unnest(${searchString}::TEXT[]) s(search)
                   WHERE lower(listing_path_query(nl.listing, a.attribute)::TEXT) LIKE
                         '%' || lower(search) || '%'))
ORDER BY l.last_seen DESC,
         LISTING_PATH_QUERY(nl.listing, ${sortCol}) ${sortDir} NULLS LAST
