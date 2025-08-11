--search
SELECT DISTINCT l.listing_id,
                l.listing,
                listing_path_query(l.listing, ${sortCol}) AS ord
FROM normalized_listings l
WHERE array_length(${searchAttributes}::TEXT[], 1) IS NULL
   OR array_length(${searchString}::TEXT[], 1) IS NULL
   OR l.listing_id IN (SELECT DISTINCT a.listing_id
                       FROM normalized_listings a
                                CROSS JOIN unnest(${searchAttributes}::TEXT[]) WITH ORDINALITY i(attribute, idx)
                                JOIN unnest(${searchString}::TEXT[]) WITH ORDINALITY s(term, idx)
                                     ON i.idx = s.idx
                       WHERE listing_path_query(a.listing, i.attribute) IS NOT NULL)
ORDER BY ord;
