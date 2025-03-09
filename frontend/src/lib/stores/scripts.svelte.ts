import type {ScriptDef, ScriptFunctionDef, ScriptSetDef} from "$lib/types/Script";


export type ScriptDefMap = { [key: number]: ScriptDef }

const transform = (data: ScriptDef[]): ScriptDefMap => data.reduce((acc, curr) => {
    acc[curr.id] = curr;
    return acc
}, {} as ScriptDefMap)


export const ScriptsStore = $state<{ value: ScriptDefMap }>({value: {}})

fetch("/api/rest/v1/fe_scripts")
    .then((response) => response.json())
    .then(transform)
    .then((data) => {
        ScriptsStore.value = data
    })


export const updateScript = (script: ScriptSetDef) =>
    fetch("/api/rest/v1/scripts", {
        method: 'put',
        body: JSON.stringify(script)
    })
        .then(r => r.json())
        .then(sf => fetch(`/api/rest/v1/fe_scripts?id=${sf.id}`))
        .then(r => r.json())
        .then(scripts => {
            const script = scripts[0]
            const existing = ScriptsStore.value!![script.id]
            ScriptsStore.value!![script.id] = {...existing, ...script}
        })

export const createScript = (script: ScriptSetDef) =>
    fetch("/api/rest/v1/scripts", {
        method: 'post',
        body: JSON.stringify(script)
    })
        .then(r => r.json())
        .then(sf => fetch(`/api/rest/v1/fe_scripts?id=${sf.id}`))
        .then(r => r.json())
        .then(scripts => {
            const script = scripts[0]
            const existing = ScriptsStore.value!![script.id]
            ScriptsStore.value!![script.id] = {...existing, ...script}
        })

export const deleteScript = (script: ScriptSetDef) =>
    fetch("/api/rest/v1/scripts", {
        method: 'delete',
        body: JSON.stringify(script)
    })
        .then(r => r.json())
        .then(nr => {
            if (nr) {
                let curr = ScriptsStore.value!!
                delete curr[script.id]
                ScriptsStore.value = curr
            }
        })

export const createScriptFunction = (scriptFun: ScriptFunctionDef) =>
    fetch("/api/rest/v1/script_functions", {
        method: 'post',
        body: JSON.stringify(scriptFun)
    })
        .then(r => r.json())
        .then(sf => fetch(`/api/rest/v1/fe_scripts?id=${sf.scriptId}`))
        .then(r => r.json())
        .then(scripts => {
            const script = scripts[0]
            console.log("found script", script)
            const existing = ScriptsStore.value!![script.id]
            console.log("existing", existing)
            ScriptsStore.value!![script.id] = {...existing, ...script}
            console.log("store updated", ScriptsStore.value)
        })

export const deleteScriptFunction = (scriptFun: ScriptFunctionDef) =>
    fetch("/api/rest/v1/script_functions", {
        method: 'delete',
        body: JSON.stringify(scriptFun)
    })
        .then(r => r.json())
        .then(() => fetch(`/api/rest/v1/fe_scripts?id=${scriptFun.scriptId}`))
        .then(r => r.json())
        .then(scripts => {
            const script = scripts[0]
            const existing = ScriptsStore.value!![script.id]
            ScriptsStore.value!![script.id] = {...existing, ...script}
        })
