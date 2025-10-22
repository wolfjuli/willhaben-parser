SELECT id, attribute, label, data_type
FROM attributes
WHERE solutions.lykos.willhaben.parser:willhaben-parser-backend:jar:1.0.0-SNAPSHOT::INT IS NULL
   OR id = solutions.lykos.willhaben.parser:willhaben-parser-backend:jar:1.0.0-SNAPSHOT::INT
