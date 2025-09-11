UPDATE attributes
SET label = ${label}::TEXT,
    data_type = ${dataType}:: TEXT ::data_type
WHERE id = ${id}:: INT
