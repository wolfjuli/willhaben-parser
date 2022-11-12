import type {Subscriber} from "svelte/types/runtime/store";
import {writable, Writable} from "svelte/store";

export abstract class BaseStore<T> {

    protected objects: Writable<T>

    protected constructor(protected storeKey: string, defaultValue: T = null) {
        this.objects = writable(localStorage.getItem(storeKey) as T)
        this.objects.subscribe(v => {
            localStorage.setItem(storeKey, v ? ("" + v as string) : (defaultValue ? "" + defaultValue : ""))
        })
    }

    subscribe(run: Subscriber<T>) {
        this.objects.subscribe(run)
    }
}