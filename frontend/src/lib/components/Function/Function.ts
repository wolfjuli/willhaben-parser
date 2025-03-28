import type {FunctionDef} from "$lib/types/Function";
import type {FunctionDefMap} from "$lib/stores/functions.svelte";

export interface FunctionProps {
    fun: FunctionDef | undefined
}

export interface FunctionDropdownProps {
    functions: FunctionDefMap
    onchange: (fun: FunctionDef) => void
}

export interface ValueProps {
    value: unknown | undefined
    name: string
}
