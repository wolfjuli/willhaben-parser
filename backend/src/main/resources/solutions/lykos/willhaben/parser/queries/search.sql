--search
WITH all_attrs AS (SELECT l.listing_id, ca.normalized, run_function(ca.function_id, '', l.listing) AS val
                   FROM custom_attributes ca
                   CROSS JOIN normalized_listings l

                   UNION ALL

                   SELECT la.listing_id, a.normalized, to_jsonb(array_to_string(la.values, ', ')) AS val
                   FROM attributes a
                   JOIN listing_attributes la
                       ON a.id = la.attribute_id

                   UNION ALL

                   SELECT l.id, 'willhabenId', to_jsonb(l.willhaben_id)
                   FROM listings l

                   UNION ALL

                   SELECT l.listing_id, 'points', to_jsonb(l.points)
                   FROM listing_points l),
     matches AS (SELECT id AS listing_id
                 FROM listings
                 WHERE array_length(${attributes}::TEXT[], 1) IS NULL
                    OR array_length(${searchString}::TEXT[], 1) IS NULL

                 UNION ALL

                 SELECT DISTINCT a.listing_id
                 FROM all_attrs a
                 JOIN unnest(${attributes}::TEXT[]) i(attribute)
                     ON a.normalized = i.attribute
                 JOIN unnest(${searchString}::TEXT[]) s(term)
                     ON a.val::TEXT LIKE '%' || s.term || '%'),
     reduced AS (SELECT a.listing_id,
                        jsonb_object_agg(a.normalized, a.val) AS listing
                 FROM all_attrs a
                 JOIN matches m
                     ON a.listing_id = m.listing_id
                 GROUP BY a.listing_id)
SELECT r.listing
FROM reduced r
ORDER BY r.listing -> ${sortcol} ${sortDir} NULLS LAST
LIMIT ${limit} OFFSET ${offset}
