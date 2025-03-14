CREATE VIEW fe_configuration AS
SELECT 'https://cache.willhaben.at/mmo' AS image_base_url,
       'https://www.willhaben.at/iad/'  AS listings_base_url
;

CREATE OR REPLACE VIEW fe_listings AS
SELECT l.willhaben_id, coalesce(lp.points, 0) AS points, jsonb_object_agg(a.normalized, la.values) AS attributes
FROM listings l
         JOIN listing_attributes la
              ON l.id = la.listing_id
         JOIN attributes a
              ON la.attribute_id = a.id
         LEFT JOIN (SELECT listing_id, sum(points) AS points
                    FROM listing_points
                    GROUP BY listing_id) lp
                   ON l.id = lp.listing_id
WHERE l.last_seen = (SELECT max(last_seen) FROM listings)
GROUP BY 1, 2
;

CREATE OR REPLACE VIEW fe_scripts AS
SELECT s.id,
       a.id AS attribute_id,
       coalesce(s.name, 'script' || s.id)                                                    AS name,
       jsonb_agg(
               jsonb_build_object('functionId', sf.function_id, 'ord', sf.ord) ORDER BY ord) AS functions
FROM scripts s
         JOIN attributes a
              ON s.attribute_id = a.id
         LEFT JOIN script_functions sf
              ON s.id = sf.script_id
GROUP BY s.id, a.id, s.name, s.id
;

