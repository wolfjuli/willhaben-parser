import type {FunctionDef} from "$lib/types/Function";

export type FunctionDefMap = { [key: number]: FunctionDef }

const transform = (data: FunctionDef[]): FunctionDefMap => data.reduce((acc, curr) => {
    acc[curr.id] = curr;
    return acc
}, {} as FunctionDefMap)

export const FunctionsStore = $state<{ value: FunctionDefMap }>({value: {}})

fetch("/api/v1/rest/functions")
    .then((response) => response.json())
    .then(data => transform(data))
    .then((data) => {
        FunctionsStore.value = data;
    })

export const updateFunction = (fun: FunctionDef) =>
    fetch("/api/v1/rest/functions", {
        method: 'put',
        body: JSON.stringify(fun)
    })
        .then(r => r.json())
        .then(fun => {
            FunctionsStore.value!![fun.id] = fun
        })

export const createFunction = (fun: FunctionDef) =>
    fetch("/api/v1/rest/functions", {
        method: 'post',
        body: JSON.stringify(fun)
    })
        .then(r => r.json())
        .then(fun => {
            FunctionsStore.value!![fun.id] = fun
        })

export const deleteFunction = (fun: FunctionDef) =>
    fetch("/api/v1/rest/functions", {
        method: 'delete',
        body: JSON.stringify(fun)
    })
        .then(r => r.json())
        .then(nr => {
            if (nr) {
                delete FunctionsStore.value!![fun.id]
            }
        })

