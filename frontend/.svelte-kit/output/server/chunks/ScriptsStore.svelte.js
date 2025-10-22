import { W as WithState, F as FetchingStore } from "./ListingsStore.svelte.js";
class ScriptsStore extends WithState {
  static #instance;
  constructor() {
    super();
    ScriptsStore.fetch();
  }
  static get instance() {
    if (!ScriptsStore.#instance) ScriptsStore.#instance = new ScriptsStore();
    return ScriptsStore.#instance;
  }
  static get value() {
    return ScriptsStore.instance.value;
  }
  static set value(newVal) {
    ScriptsStore.instance.value = newVal;
  }
  static fetch(id = void 0) {
    FetchingStore.whileFetching("scripts", () => {
      const filter = id ? `?id=${id}` : "";
      return fetch(`/api/rest/v1/scripts/full${filter}`).then((response) => response.json()).then((data) => {
        if (!id) ScriptsStore.value = {};
        data.forEach((d) => {
          ScriptsStore.value[d.id] = d;
        });
      });
    });
  }
  static update(script) {
    fetch("/api/rest/v1/scripts", {
      method: "put",
      body: JSON.stringify(script)
    }).then(() => ScriptsStore.fetch(script.id));
  }
  static create(script) {
    fetch("/api/rest/v1/scripts", {
      method: "post",
      body: JSON.stringify(script)
    }).then(() => ScriptsStore.fetch(script.id));
  }
  static delete(script) {
    fetch("/api/rest/v1/scripts", {
      method: "post",
      body: JSON.stringify(script)
    }).then(() => delete ScriptsStore.value[script.id]);
  }
  static createScriptFunction = (scriptFun) => fetch("/api/rest/v1/script_functions", {
    method: "post",
    body: JSON.stringify(scriptFun)
  }).then(() => ScriptsStore.fetch(scriptFun.scriptId));
  static deleteScriptFunction = (scriptFun) => fetch("/api/rest/v1/script_functions", {
    method: "delete",
    body: JSON.stringify(scriptFun)
  }).then(() => ScriptsStore.fetch(scriptFun.scriptId));
}
export {
  ScriptsStore as S
};
