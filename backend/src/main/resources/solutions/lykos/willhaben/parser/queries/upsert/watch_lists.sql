WITH inp(id, url) AS (VALUES ${allValues}),
     ins AS (
         INSERT INTO watch_lists (url)
             SELECT url
             FROM inp
             WHERE id IS NULL
             RETURNING id, url),
     upd AS (
         UPDATE watch_lists wl
             SET url = i.url
             FROM inp i
             WHERE i.id = wl.id
             RETURNING id, url)
SELECT *
FROM ins

UNION ALL

SELECT *
FROM upd

