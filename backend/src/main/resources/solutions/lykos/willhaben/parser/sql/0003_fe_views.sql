CREATE VIEW fe_configuration AS
SELECT 'https://cache.willhaben.at/mmo' AS image_base_url,
       'https://www.willhaben.at/iad/'  AS listings_base_url
;

CREATE VIEW fe_listings AS
SELECT l.willhaben_id, jsonb_object_agg(a.attribute, la.values) AS attributes
FROM listings l
         JOIN data_blocks d
              ON l.id = d.listing_id
         JOIN listing_attributes la
              ON l.id = la.listing_id
         JOIN attributes a
              ON la.attribute_id = a.id
WHERE d.timestamp = (SELECT max(timestamp) FROM data_blocks)
GROUP BY 1
;

CREATE VIEW fe_scripts AS
SELECT s.id,
       a.normalized                                                                          AS attribute,
       coalesce(s.name, 'script' || s.id)                                                    AS name,
       jsonb_agg(
               jsonb_build_object('functionId', sf.function_id, 'ord', sf.ord) ORDER BY ord) AS functions
FROM scripts s
         JOIN attributes a
              ON s.attribute_id = a.id
         JOIN script_functions sf
              ON s.id = sf.script_id
GROUP BY s.id, a.normalized, s.name, s.id
;
