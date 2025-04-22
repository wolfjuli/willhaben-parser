CREATE OR REPLACE FUNCTION toggle_triggers(action TEXT, namespace TEXT = 'willhaben')
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
DECLARE
    r RECORD;
BEGIN
    FOR r IN SELECT c.relname, t.tgname
             FROM pg_namespace n
             JOIN pg_class c
                 ON c.relnamespace = n.oid
             JOIN pg_trigger t
                 ON t.tgrelid = c.oid
             WHERE n.nspname = namespace
               AND t.tgname LIKE 'trg_changed%'
        LOOP
            EXECUTE format('alter table %I.%I %s trigger %s', namespace, r.relname, action, r.tgname);
        END LOOP;
END;
$$
;



ALTER TABLE listing_custom_attributes
    DROP CONSTRAINT IF EXISTS listing_custom_attributes_listing_id_fkey,
    ADD FOREIGN KEY (listing_id) REFERENCES listings (id) ON DELETE CASCADE ON UPDATE CASCADE;

SELECT toggle_triggers('disable');

DELETE
FROM normalized_listings
WHERE listing_id NOT IN (SELECT id FROM listings);

SELECT toggle_triggers('enable');

ALTER TABLE normalized_listings
    DROP CONSTRAINT IF EXISTS normalized_listings_listing_id_fkey,
    ADD FOREIGN KEY (listing_id) REFERENCES listings (id) ON DELETE CASCADE ON UPDATE CASCADE;

