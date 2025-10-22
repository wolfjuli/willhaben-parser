import type {ScriptDef} from "$lib/types/Script";
import type {Attribute} from "$lib/types/Attribute";
import type {Listing} from "$lib/types/Listing";
import type {FunctionDefMap} from "$lib/stores/functions.svelte";

export interface CreateScriptProps {
    attributes: Attribute[]
}

export interface ScriptProps {
    script: ScriptDef
    attributes: Attribute[]
}


export interface ScriptFunctionsProps {
    script: ScriptDef
    attributes: Attribute[]
    listing: Listing | undefined
    functions: FunctionDefMap
}
