import type {ScriptDef, ScriptFunctionDef, ScriptSetDef} from "$lib/types/Script";
import {WithState} from "$lib/stores/WithState.svelte";
import {FetchingStore} from "$lib/stores/FetchingStore.svelte";


export type ScriptDefMap = { [key: number]: ScriptDef }

export class ScriptsStore extends WithState<ScriptDefMap> {
    static #instance: ScriptsStore

    private constructor() {
        super();

        ScriptsStore.fetch()
    }

    static get instance(): ScriptsStore {
        if (!ScriptsStore.#instance)
            ScriptsStore.#instance = new ScriptsStore()

        return ScriptsStore.#instance
    }

    static get value(): ScriptDefMap {
        return ScriptsStore.instance.value
    }

    static set value(newVal: ScriptDefMap) {
        ScriptsStore.instance.value = newVal
    }

    static fetch(id: number | undefined = undefined) {
        FetchingStore.whileFetching("scripts", () => {
            const filter = id ? `?id=${id}` : ''
            return fetch(`/api/v1/rest/scripts${filter}`)
                .then((response) => response.json())
                .then((data: ScriptDef[]) => {
                    if (!id) ScriptsStore.value = {}
                    data.forEach(d => {
                        ScriptsStore.value[d.id] = d
                    })
                })
        })
    }

    static update(script: ScriptSetDef) {
        fetch("/api/v1/rest/scripts", {
            method: 'put',
            body: JSON.stringify(script)
        }).then(() => ScriptsStore.fetch(script.id))
    }

    static create(script: ScriptSetDef) {
        fetch("/api/v1/rest/scripts", {
            method: 'post',
            body: JSON.stringify(script)
        }).then(() => ScriptsStore.fetch(script.id))
    }

    static delete(script: ScriptSetDef) {
        fetch("/api/v1/rest/scripts", {
            method: 'post',
            body: JSON.stringify(script)
        }).then(() => delete ScriptsStore.value[script.id])
    }


    static createScriptFunction = (scriptFun: ScriptFunctionDef) =>
        fetch("/api/v1/rest/script_functions", {
            method: 'post',
            body: JSON.stringify(scriptFun)
        })
            .then(() => ScriptsStore.fetch(scriptFun.scriptId))

    static deleteScriptFunction = (scriptFun: ScriptFunctionDef) =>
        fetch("/api/v1/rest/script_functions", {
            method: 'delete',
            body: JSON.stringify(scriptFun)
        })
            .then(() => ScriptsStore.fetch(scriptFun.scriptId))
}
