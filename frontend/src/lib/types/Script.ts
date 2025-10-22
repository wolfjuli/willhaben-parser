export type ScriptDef = {
    id: number,
    name: string,
    attributeId: number,
    functions: { ord: number, functionId: number }[]
}

export type ScriptSetDef = {
    id: number,
    name: string,
    attributeId: number
}

export type ScriptFunctionDef = {
    scriptId: number,
    functionId: number,
    ord: number
}
