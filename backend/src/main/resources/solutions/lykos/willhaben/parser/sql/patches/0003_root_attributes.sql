CREATE OR REPLACE FUNCTION listing_path_query(listing jsonb, path TEXT)
    RETURNS jsonb
    LANGUAGE plpgsql
    IMMUTABLE
AS
$$
DECLARE
    ret jsonb;
BEGIN
    CASE
        WHEN left(path, 1) = '$' THEN SELECT u.val AS val
                                                        INTO ret
                                                        FROM jsonb_path_query(listing, (path)::jsonpath)
                                                            WITH ORDINALITY u(val, idx)
                                                        LIMIT 1;
        ELSE SELECT coalesce(u.val, c.val, b.val)
             INTO ret
             FROM jsonb_path_query(listing, ('$.user.' || path)::jsonpath) WITH ORDINALITY u(val, idx)
             FULL JOIN jsonb_path_query(listing, ('$.custom.' || path)::jsonpath) WITH ORDINALITY c(val, idx)
                 USING (idx)
             FULL JOIN jsonb_path_query(listing, ('$.base.' || path)::jsonpath) WITH ORDINALITY b(val, idx)
                 USING (idx);
        END CASE;

    return ret;
END
$$;

INSERT INTO attributes ( attribute, label, sorting_attribute, data_type) VALUES ( '$.points', 'Punkte', null, 'NUMBER');
