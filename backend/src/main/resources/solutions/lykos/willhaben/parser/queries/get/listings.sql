SELECT l.willhaben_id, jsonb_object_agg(a.attribute, la.values) AS attributes
FROM listings l
         JOIN data_blocks d
              ON l.id = d.listing_id
         JOIN listing_attributes la
              ON l.id = la.listing_id
         JOIN attributes a
              ON la.attribute_id = a.id
--WHERE d.timestamp = (SELECT max(timestamp) FROM data_blocks)
GROUP BY 1
