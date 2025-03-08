SELECT f.id, function, coalesce(f.name, 'fun' || id) AS name
FROM functions f
;
