CREATE
OR REPLACE
VIEW fe_listings AS
SELECT l.id,
       l.willhaben_id,
       coalesce(lp.points, 0)                                         AS points,
       jsonb_object_agg(a.normalized, coalesce(ua.values, la.values)) AS attributes
FROM listings l
JOIN listing_attributes la
    ON l.id = la.listing_id
JOIN all_attributes a
    ON la.attribute_id = a.id
LEFT JOIN (SELECT listing_id, sum(points) AS points
           FROM listing_points
           GROUP BY listing_id) lp
    ON l.id = lp.listing_id
LEFT JOIN user_defined_attributes ua
    ON la.listing_id = ua.listing_id
    AND la.attribute_id = ua.listing_id
WHERE l.last_seen = (SELECT max(last_seen) FROM listings)
GROUP BY 1, 2, 3
ORDER BY points DESC
;