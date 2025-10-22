SELECT id, attribute, label, data_type
FROM attributes
WHERE ${id}::INT IS NULL
   OR id = ${id}::INT
