--attributes
SELECT id, attribute, label, data_type
FROM attributes
WHERE array_length(${ids}::INT[], 1) IS NULL
   OR id = ANY (${ids}::INT[])
