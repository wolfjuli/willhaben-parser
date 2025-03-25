TRUNCATE TABLE scripts CASCADE;
ALTER SEQUENCE scripts_id_seq RESTART;

TRUNCATE TABLE functions CASCADE;
ALTER SEQUENCE functions_id_seq RESTART;

INSERT INTO scripts (attribute_id, name)
VALUES (3, 'Gute Wörter Titel');
INSERT INTO scripts (attribute_id, name)
VALUES ((SELECT id FROM attributes WHERE normalized = 'description'), 'Gute Wörter desc');
INSERT INTO scripts (attribute_id, name)
VALUES (3, 'Schlechte Wörter Titel');
INSERT INTO scripts (attribute_id, name)
VALUES ((SELECT id FROM attributes WHERE normalized = 'description'), 'Schlechte Wörter desc');
INSERT INTO scripts (attribute_id, name)
VALUES (55, 'Preis');
INSERT INTO scripts (attribute_id, name)
VALUES (35, 'Location');
INSERT INTO scripts (attribute_id, name)
VALUES (48, 'Wohnfläche Haus');
INSERT INTO scripts (attribute_id, name)
VALUES (48, 'Wohnfläche Wohnung');
INSERT INTO scripts (attribute_id, name)
VALUES (50, 'Zimmer < 3,5');

INSERT INTO functions (function, name)
VALUES ('(val, row) => val / 200000', '/ 200 000');
INSERT INTO functions (function, name)
VALUES ('(val, row) => val * 2', 'Prio 2');
INSERT INTO functions (function, name)
VALUES ('(val, row) => val * 10', 'Prio 10');
INSERT INTO functions (function, name)
VALUES ('(val, row) => "47.067393,15.442110," + val', 'Add Graz Jakominiplatz');
INSERT INTO functions (function, name)
VALUES (e'(val, row) => {
  const [lat1, lon1, lat2, lon2] = val?.split(",");
  const radLat1 = lat1 * (Math.PI / 180);
  const radLon1 = lon1 * (Math.PI / 180);
  const radLat2 = lat2 * (Math.PI / 180);
  const radLon2 = lon2 * (Math.PI / 180);

  // Equirectangular formula
  const earthRadius = 6371000
  const x = (radLon2 - radLon1) * Math.cos(0.5 * (radLat2 + radLat1))
  const y = radLat2 - radLat1
  return earthRadius * Math.sqrt(x * x + y * y)
}', 'Distance between 2 Points');

INSERT INTO functions (function, name)
VALUES ('(val, row) => Math.tanh(val)', 'tanh');
INSERT INTO functions (function, name)
VALUES ('(val, row) => Math.pow(Math.E, -(val * val) / 0.32) / Math.sqrt(2 * Math.PI * 0.16)',
        'Normalverteilung (sig=0.4)');
INSERT INTO functions (function, name)
VALUES ('(val, row) =>  (+val - 100) / 100', 'Wohnfläche Wohnung');
INSERT INTO functions (function, name)
VALUES ('(val, row) =>  (500000 - val) / 500000', 'Preis normalisiert');
INSERT INTO functions (function, name)
VALUES ('(val, row) => +(["wohnrecht", "bauherrenmodell", "anlageobjekt", "anlegerwohnung", "vermietet", " bms ", "beiteiligungsprojekt"].some(e => val.toLowerCase().includes(e))) * -1',
        'Schlechte Wörter');
INSERT INTO functions (function, name)
VALUES ('(val, row) => +(["fernblick", "aussicht", "weitblick", "fußbodenheizung", "fussbodenheizung", "garage"].some(e => val.toLowerCase().includes(e)))',
        'Gute Wörter');
INSERT INTO functions (function, name)
VALUES ('(val, row) => ({href: row["seoUrl"], value: row["heading"]})', 'Attr:Link');
INSERT INTO functions (function, name)
VALUES ('(val, row) => +row["price"] / Math.max(+`${row["area"] ?? 0}`.replace(",", ".") , +`${row["livingArea"] ?? 0}`.replace(",", "."))',
        'Preis/m2');
INSERT INTO functions (function, name)
VALUES ('(val, row) => val * +(["Loft/Studio","Garconniere","Rohdachboden","Maisonette"].includes(row["propertyType"] ?? "") || (row["propertyType"] ?? "").toString().toLowerCase().includes("wohnung"))',
        'ist Wohnung');
INSERT INTO functions (function, name)
VALUES ('(val, row) => val * +(["Villa","Bungalow","Rohbau"].includes(row["propertyType"] ?? "") || (row["propertyType"] ?? "").toString().toLowerCase().includes("haus"))',
        'ist Haus');
INSERT INTO functions (function, name)
VALUES ('(val, row) =>  (val - 150) / 150', 'Wohnfläche Haus');
INSERT INTO functions (function, name)
VALUES ('(val, row) => +(+`${val ?? ""}`.split("X")[0] < 3.5) * -1', 'Zimmer < 3.5');
INSERT INTO functions (function, name)
VALUES ('(val, row) => "-"', 'Attr:Leer');
INSERT INTO functions (function, name)
VALUES ('(val, row) => val', 'Wohnung Aussenanlagen');

INSERT INTO willhaben.script_functions (script_id, function_id, ord)
VALUES (1, 11, 1),
       (1, 2, 2),
       (2, 11, 1),
       (2, 2, 2),
       (3, 10, 1),
       (3, 3, 2),
       (4, 10, 1),
       (4, 3, 2),
       (5, 9, 1),
       (5, 7, 2),
       (5, 3, 3),
       (6, 4, 1),
       (6, 5, 2),
       (6, 7, 3),
       (6, 6, 4),
       (6, 3, 5),
       (7, 16, 1),
       (7, 7, 2),
       (7, 15, 3),
       (7, 3, 4),
       (8, 8, 1),
       (8, 7, 2),
       (8, 14, 3),
       (8, 3, 4),
       (9, 17, 1),
       (9, 2, 2);


INSERT INTO willhaben.custom_attributes (normalized, label, function_id, data_type)
VALUES ('seoUrl', 'Link', 12, 'LINK'),
       ('pricePerArea', 'Preis/m2', 13, 'TEXT'),
       ('notes', 'Notiz', 18, 'TEXT');

CREATE OR REPLACE VIEW folded_scripts AS
SELECT script_id,
       attribute_id,
       '(v, r) => {' || funs || ' return ' || folded || 'v' || repeat(', r)', count::INT) || '; }' AS script
FROM (SELECT s.id                                                                      AS script_id,
             s.attribute_id,
             count(*)                                                                  AS count,
             string_agg(DISTINCT 'const fun' || f.id || ' = ' || function || '; ', '') AS funs,
             string_agg('fun' || f.id || '(', '' ORDER BY sf.ord DESC)                 AS folded
      FROM scripts s
      JOIN script_functions sf
          ON s.id = sf.script_id
      JOIN functions f
          ON sf.function_id = f.id
      GROUP BY s.id, s.attribute_id) x
;

SELECT update_listing_points();


