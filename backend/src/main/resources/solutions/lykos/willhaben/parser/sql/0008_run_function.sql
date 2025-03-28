CREATE OR REPLACE FUNCTION run_function(_function TEXT, attribute TEXT, listing jsonb)
    RETURNS jsonb
    LANGUAGE plv8
AS
$$
let fun = eval(_function);

return fun(attribute, listing);
$$;

CREATE OR REPLACE FUNCTION run_function(function_id SMALLINT, attribute TEXT, listing jsonb)
    RETURNS jsonb
    LANGUAGE plv8
AS
$$
let fun = eval(plv8
    .execute("select function from functions where id = $1", [function_id])[0]
    .function
);

return fun(attribute, listing);
$$
