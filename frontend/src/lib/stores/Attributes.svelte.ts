import type {BaseAttribute} from "$lib/types/Attribute";
import {WithState} from "$lib/stores/WithState.svelte";

export class BaseAttributesStore extends WithState<BaseAttribute[]> {
    static #instance: BaseAttributesStore

    private constructor() {
        super([]);

        fetch("/api/rest/v1/attributes")
            .then(r => r.json())
            .then(d => BaseAttributesStore.instance.value = d)
    }

    static get instance(): BaseAttributesStore {
        if (!this.#instance)
            this.#instance = new BaseAttributesStore()

        return this.#instance
    }

    static get value(): BaseAttribute[] {
        return BaseAttributesStore.instance.value
    }

    static async fetch(id: number | undefined = undefined): Promise<void> {
        const url = '/api/rest/v1/attributes' + (id ? `/${id}` : '')
        fetch(url)
            .then(r => r.json())
            .then(d => {
                if (id) {
                    BaseAttributesStore.instance.value = [...BaseAttributesStore.instance.value.filter(b => b.id !== id), d]
                } else {
                    BaseAttributesStore.instance.value = d
                }
            })
    }

    static async updateAttribute(attr: BaseAttribute) {
        await fetch(`/api/rest/v1/attributes/${attr.id}`, {
            method: 'post',
            headers: {
                "content-type": "application/json"
            },
            body: JSON.stringify(attr)
        }).then(() => BaseAttributesStore.fetch(attr.id))
    }
}
