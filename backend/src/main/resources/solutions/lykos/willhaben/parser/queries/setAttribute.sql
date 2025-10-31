--setAttribute
INSERT INTO attributes(attribute, label, data_type, sorting_attribute)
VALUES (${attribute}::TEXT, ${label}::TEXT, ${dataType}::TEXT::data_type, ${sortBy}::INT)
ON CONFLICT (attribute) DO UPDATE
    SET label     = excluded.label,
        data_type = excluded.data_type,
        sorting_attribute = excluded.sorting_attribute
RETURNING id, attribute, label, data_type, sorting_attribute;