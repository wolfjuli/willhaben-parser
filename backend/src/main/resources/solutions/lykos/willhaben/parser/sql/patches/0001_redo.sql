ALTER TABLE attributes
    RENAME TO attributes_old;
ALTER TABLE custom_attributes
    RENAME TO custom_attributes_old;
ALTER TABLE user_defined_attributes
    RENAME TO listing_user_attributes;


DROP TABLE user_defined_locations;
DROP TABLE listing_locations;
DROP TABLE locations;
DROP TABLE listing_attributes;
DROP TABLE listing_custom_attributes;



CREATE TABLE attributes
(
    id        SERIAL PRIMARY KEY,
    attribute TEXT NOT NULL UNIQUE,
    label     TEXT,
    data_type DATA_TYPE
);


CREATE TABLE custom_attributes
(
    id          INT PRIMARY KEY NOT NULL REFERENCES attributes (id) ON UPDATE CASCADE ON DELETE CASCADE,
    function_id SMALLINT        NOT NULL REFERENCES functions ON UPDATE CASCADE ON DELETE CASCADE,
    data_type   DATA_TYPE       NOT NULL
);

INSERT INTO attributes(attribute, label, data_type)
SELECT attribute, label, data_type
FROM attributes_old;

INSERT INTO attributes(attribute, label, data_type)
SELECT attribute, label, data_type
FROM custom_attributes_old
;
INSERT INTO custom_attributes(id, function_id, data_type)
SELECT (SELECT id FROM attributes WHERE attribute = c.attribute),
       function_id,
       data_type
FROM custom_attributes_old c
;

ALTER TABLE scripts
    ADD ey (attribute_id) REFERENCES attributes(id) ON DELETE CASCADE ON UPDATE CASCADE;
;
DROP TABLE attribute_mapping CASCADE;
DROP TABLE attributes_old CASCADE;
DROP TABLE custom_attributes_old CASCADE;



CREATE OR REPLACE VIEW normalized_listings(listing_id, willhaben_id, listing, md5) AS
WITH ca AS (SELECT l.id                                                                                       AS listing_id,
                   jsonb_object_agg(a.attribute,
                                    run_function(f.function, a.attribute, jsonb_build_object('base', l.raw))) AS raw
            FROM listings l
                     CROSS JOIN custom_attributes ca
                     JOIN attributes a
                          ON a.id = ca.id
                     JOIN functions f
                          ON f.id = ca.function_id
            GROUP BY l.id),
     ua AS (SELECT lua.listing_id,
                   jsonb_object_agg(a.attribute, lua."values") AS raw
            FROM listing_user_attributes lua
                     JOIN attributes a
                          ON a.id = lua.attribute_id
            GROUP BY lua.listing_id)
SELECT l.id                                                                           AS listing_id,
       l.willhaben_id,
       jsonb_build_object('base', l.raw, 'custom', ca.raw, 'user', ua.raw)            AS listing,
       md5(jsonb_build_object('base', l.raw, 'custom', ca.raw, 'user', ua.raw)::TEXT) AS md5
FROM listings l
         LEFT JOIN ca
                   ON l.id = ca.listing_id
         LEFT JOIN ua
                   ON l.id = ua.listing_id;

