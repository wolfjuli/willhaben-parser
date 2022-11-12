import {writable, Writable} from "svelte/store";
import type {Subscriber} from "svelte/types/runtime/store";

export abstract class BaseVolatileStore<T> {

    protected objects: Writable<T>

    protected constructor(defaultValue: T = null) {
        this.objects = writable(defaultValue)
    }

    subscribe(run: Subscriber<T>) {
        this.objects.subscribe(run)
    }
}