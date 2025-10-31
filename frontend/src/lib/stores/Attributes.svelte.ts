import type {BaseAttribute} from "$lib/types/Attribute";
import {WithState} from "$lib/stores/WithState.svelte";
import {Socket} from "$lib/api/Socket";

export class BaseAttributesStore extends WithState<BaseAttribute[]> {
    static #instance: BaseAttributesStore

    private constructor() {
        super([]);
        Socket.register("getAttributes", BaseAttributesStore.upsert)
        Socket.register("setAttribute", (it: BaseAttribute) => BaseAttributesStore.upsert([it]))
        Socket.send("getAttributes", {})
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

    static set(attr: BaseAttribute): BaseAttribute {
        Socket.send("setAttribute", attr)
        return attr
    }
}
