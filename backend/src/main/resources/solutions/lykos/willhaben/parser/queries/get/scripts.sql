SELECT s.id,
       a.normalized                                                                          AS attribute,
       coalesce(s.name, 'script' || s.id)                                                    AS name,
       jsonb_agg(
               jsonb_build_object('functionId', sf.function_id, 'ord', sf.ord) ORDER BY ord) AS functions
FROM scripts s
         JOIN attributes a
              ON s.attribute_id = a.id
         JOIN script_functions sf
              ON s.id = sf.script_id
GROUP BY s.id, a.normalized, s.name, s.id
