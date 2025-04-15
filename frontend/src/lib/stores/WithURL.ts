import {WithState} from "$lib/stores/WithState.svelte";

export function WithURL<T, N extends { [key: string]: string | number | object }>(
    refreshUrl: string,
    createUrl: string,
    updateUrl: string,
    deleteUrl: string,
    id: (v: N) => string | number = v => v.id as (string | number)
) {
    abstract class WithURL extends WithState<T> {


        refresh(props: { [key: string]: (string | number | (string | number)[]) }) {
            const filter = Object.keys(props).length ? "?" + Object.keys(props).reduce((acc, k) => {
                return acc + `${k}=${props[k]}`
            }, "") : ""
            fetch(refreshUrl + filter, {
                method: 'get',
            })
                .then(r => r.json())
                .then(d => this.value = d)
        }

        create(newValue: N) {
            fetch(createUrl, {
                method: 'post',
                body: JSON.stringify(newValue)
            })
                .then(() => this.refresh(newValue))

        }

        update(newValue: N): T {
            throw new Error("create() has not been implemented!")
        }

        delete(newValue: N): T {
            throw new Error("create() has not been implemented!")
        }
    }

    return WithURL
}
