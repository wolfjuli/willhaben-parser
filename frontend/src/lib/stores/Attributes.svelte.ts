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

    static upsert(attributes: BaseAttribute[]): BaseAttribute[] {
        BaseAttributesStore.instance.value = [
            ...(BaseAttributesStore.instance.value.filter(a => !attributes.find(n => n.id === a.id))),
            ...attributes
        ]

        return BaseAttributesStore.instance.value
    }

    static updateAttribute(attr: BaseAttribute): BaseAttribute {
        BaseAttributesStore.instance.value = [
            ...(BaseAttributesStore.instance.value.filter(a => a.id !== attr.id)),
            attr
        ]
        return attr
    }
}
