import type {Writable} from "svelte/store";
import type {Subscriber} from "svelte/types/runtime/store";
import {BaseVolatileStore} from "./BaseVolatileStore";
import {logDebug, logError} from "../modules/Extensions";


const options = {};

export abstract class BaseRequestStoreStore<T> extends BaseVolatileStore<T> {

    protected objects: Writable<T>


    protected constructor(defaultValue: T = null, protected url: string) {
        super(defaultValue)

        this.refresh()
    }

    protected dataTransformer(data: any): T {
        return data
    }

    subscribe(run: Subscriber<T>) {
        this.objects.subscribe(run)
    }

    refresh() {
        fetch(this.url, options)
            .then(response => {
                logDebug(response)
                return response.json()
            })
            .then(this.dataTransformer)
            .then(data => {
                this.objects.set(data);
            }).catch(error => {
            logError(error);
            return [];
        });
    }
}