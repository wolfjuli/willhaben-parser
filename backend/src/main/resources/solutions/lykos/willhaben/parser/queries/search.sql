--search
WITH all_attrs(listing_id, normalized, value) AS (SELECT l.id, 'listingId', to_jsonb(l.id) AS value
                                                  FROM listings l
                                                  WHERE l.last_seen = (SELECT max(last_seen) FROM listings)

                                                  UNION ALL

                                                  SELECT l.id, 'willhabenId', to_jsonb(l.willhaben_id)
                                                  FROM listings l
                                                  WHERE l.last_seen = (SELECT max(last_seen) FROM listings)

                                                  UNION ALL

                                                  SELECT la.listing_id,
                                                         a.normalized,
                                                         to_jsonb(array_to_string(la.values, ', '))
                                                  FROM attributes a
                                                  JOIN listing_attributes la
                                                      ON a.id = la.attribute_id
                                                  JOIN listings l
                                                      ON l.id = la.listing_id
                                                  WHERE l.last_seen = (SELECT max(last_seen) FROM listings)


                                                  UNION ALL

                                                  SELECT lp.listing_id, 'points', to_jsonb(sum(lp.points))
                                                  FROM listing_points lp
                                                  JOIN listings l
                                                      ON l.id = lp.listing_id
                                                  WHERE l.last_seen = (SELECT max(last_seen) FROM listings)
                                                  GROUP BY lp.listing_id

                                                  UNION ALL

                                                  SELECT nl.listing_id,
                                                         ca.normalized,
                                                         run_function(ca.function_id, '', nl.listing)
                                                  FROM custom_attributes ca
                                                  CROSS JOIN normalized_listings nl
                                                  JOIN listings l
                                                      ON l.id = nl.listing_id
                                                  WHERE l.last_seen = (SELECT max(last_seen) FROM listings)),
     matches AS (SELECT DISTINCT a.listing_id, a.value AS ord
                 FROM all_attrs a
                 WHERE a.normalized = ${sortCol}
                   AND (
                     array_length(${searchAttributes}::TEXT[], 1) IS NULL OR
                     array_length(${searchString}::TEXT[], 1) IS NULL OR
                     a.listing_id IN (SELECT DISTINCT a.listing_id
                                      FROM all_attrs a
                                      JOIN unnest(${searchAttributes}::TEXT[]) i(attribute)
                                          ON a.normalized = i.attribute
                                      JOIN unnest(${searchString}::TEXT[]) s(term)
                                          ON a.value::TEXT LIKE '%' || s.term || '%')
                     ))
SELECT a.listing_id,
       m.ord,
       jsonb_object_agg(a.normalized, a.value) AS listing
FROM all_attrs a
JOIN matches m
    ON a.listing_id = m.listing_id
JOIN unnest(${viewAttributes}::TEXT[]) v(attr)
    ON a.normalized = v.attr
GROUP BY a.listing_id, m.ord
ORDER BY m.ord ${sortDir}
