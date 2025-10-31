import type {ScriptDef, ScriptFunctionDef, ScriptSetDef} from "$lib/types/Script";
import {WithState} from "$lib/stores/WithState.svelte";
import {Socket} from "$lib/api/Socket";


export type ScriptDefMap = { [key: number]: ScriptDef }

export class ScriptsStore extends WithState<ScriptDefMap> {
    static #instance: ScriptsStore

    private constructor() {
        super({});

        Socket.register("getScripts", this.upsert)
        Socket.register("setScript", (it: ScriptDef) => this.upsert([it]))
    }

    static get instance(): ScriptsStore {
        if (!ScriptsStore.#instance)
            ScriptsStore.#instance = new ScriptsStore()

        return ScriptsStore.#instance
    }

    static get value(): ScriptDefMap {
        return ScriptsStore.instance.value
    }

    static upsert(scripts: ScriptDef[]) {
        scripts.forEach(d => {
            ScriptsStore.value[d.id] = d
        })
    }

    static update(script: ScriptSetDef) {
        Socket.send("setScript", script)
    }

    static create(script: ScriptSetDef) {
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
