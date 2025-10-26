import {WithState} from "$lib/stores/WithState.svelte";

export type GenericObject = { [key: string]: (string | number | ((string | number)[]) | object) }

export function WithURL<T extends GenericObject, N extends GenericObject>(
    refreshUrl: string,
    createUrl: string = refreshUrl,
    updateUrl: string = createUrl,
    deleteUrl: string = createUrl,
    id: (v: N | T) => string | number = v => v.id as (string | number)
) {
    class WithURL extends WithState<{ [key: string]: T }> {
        refresh(props: GenericObject) {
            const filter = Object.keys(props).length ? "?" + Object.keys(props).reduce((acc, k) => {
                return acc + `${k}=${props[k]}`
            }, "") : ""
            fetch(refreshUrl + filter, {
                method: 'get',
            })
                .then(r => r.json())
                .then((data: T[]) => {
                        if (!filter) this.value = {}
                        data.forEach(d => {
                            this.value[id(d)] = d
                        })
                    }
                )
        }

        create(newValue: N) {
            fetch(createUrl, {
                method: 'post',
                body: JSON.stringify(newValue)
            })
                .then(() => this.refresh(newValue))

        }

        update(newValue: N) {
            fetch(updateUrl, {
                method: 'put',
                body: JSON.stringify(newValue)
            })
                .then(() => this.refresh(newValue))
        }

        delete(newValue: N) {
            fetch(deleteUrl, {
                method: 'delete',
                body: JSON.stringify(newValue)
            })
                .then(() => this.refresh(newValue))
        }
    }

    return WithURL
}
