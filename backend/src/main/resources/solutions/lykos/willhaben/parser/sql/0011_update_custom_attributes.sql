ROLLBACK;
BEGIN;
DROP FUNCTION IF EXISTS update_listing_custom_attributes;

CREATE OR REPLACE FUNCTION update_listing_custom_attributes(willhaben_ids INT[] = NULL, attribute_ids SMALLINT[] = NULL,
                                                            listing_ids INT[] = NULL, function_ids SMALLINT[] = NULL)
    RETURNS VOID
    LANGUAGE plv8
AS
$$
const plan = plv8.prepare('SELECT ca.id, f.function FROM custom_attributes ca JOIN functions f ON ca.function_id = f.id WHERE ($1 IS NULL OR ca.id = any($1)) AND ($2 IS NULL OR f.id =any($2))', ['smallint[]', 'smallint[]']);
let cursor = plan.cursor([attribute_ids, function_ids]);
let funs = []
let row;

while (row = cursor.fetch()) {
    funs = [...funs, {id: row.id, fun: eval(row.function)}]
}

cursor.close()
plan.free()

const entries = plv8.prepare(
    'SELECT listing_id, l.listing FROM normalized_listings l  WHERE ($1 IS NULL OR  listing_id IN (SELECT id FROM listings WHERE willhaben_id = ANY ($1)))  AND ($2 IS NULL OR listing_id = ANY ($2))',
    ['int[]', 'int[]']
);

cursor = entries.cursor([willhaben_ids, listing_ids]);
while (row = cursor.fetch()) {
    for (let i = 0; i < funs.length; i++) {
        plv8.execute(
            'INSERT INTO listing_custom_attributes(listing_id, attribute_id, values) VALUES($1, $2, $3) ON CONFLICT(listing_id, attribute_id) DO UPDATE SET values = excluded.values',
            [row.listing_id, funs[i].id, funs[i].fun('', row.listing)]
        );
    }
}
$$;


SELECT id
FROM listings
WHERE last_seen = (SELECT max(last_seen) FROM listings)



SELECT update_listing_custom_attributes(listing_ids := ARRAY [40037,18017,3715,57628,17432,53666,18834,3713,68175,57850,18823,27663,26091])
;
