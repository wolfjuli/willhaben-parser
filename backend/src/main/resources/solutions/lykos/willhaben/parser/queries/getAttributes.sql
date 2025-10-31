--getAttributes
SELECT a.id, a.attribute, a.label, a.data_type, s.attribute AS sorting_attribute
FROM attributes a
LEFT JOIN attributes s
    ON a.sorting_attribute = s.id
WHERE array_length(${ids}::INT[], 1) IS NULL
   OR a.id = ANY (${ids}::INT[])
