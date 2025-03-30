--search
WITH all_attrs AS (SELECT l.listing_id, ca.normalized, run_function(ca.function_id, '', l.listing) AS value
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

                   SELECT l.listing_id, 'points', to_jsonb(sum(l.points))
                   FROM listing_points l
                   GROUP BY l.listing_id),
     matches AS (SELECT DISTINCT a.listing_id, a.value AS ord
                 FROM all_attrs a
                 WHERE a.normalized = ${sortCol}
                   AND (
                     array_length(${searchAttributes}::TEXT[], 1) IS NULL OR
                     array_length(${searchString}::TEXT[], 1) IS NULL
                     )

                 UNION ALL

                 SELECT DISTINCT a.listing_id, a.value
                 FROM all_attrs a
                 JOIN unnest(${searchAttributes}::TEXT[]) i(attribute)
                     ON a.normalized = i.attribute
                 JOIN unnest(${searchString}::TEXT[]) s(term)
                     ON a.value::TEXT LIKE '%' || s.term || '%'
                 WHERE a.normalized = ${sortCol})
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
