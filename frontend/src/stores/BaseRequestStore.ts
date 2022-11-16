import type {Writable} from "svelte/store";
import type {Subscriber} from "svelte/types/runtime/store";
import {BaseVolatileStore} from "./BaseVolatileStore";
import {logDebug, logError, trickleCopy} from "../modules/Extensions";


const options = {};

export abstract class BaseRequestStoreStore<T> extends BaseVolatileStore<T> {

    protected objects: Writable<T>

    protected initialized: Boolean = false

    protected constructor(defaultValue: T = null, protected url: string) {
        super(defaultValue)

    }

    protected dataTransformer(data: any): T {
        return data
    }

    protected subscribers = []
    protected trickleTimer = null

    subscribe(run: Subscriber<T>, trickle: Boolean = true) {
        if(!this.initialized)
            this.refresh()

        this.subscribers.push(run)
        this.objects.subscribe(v => {
            if (trickle && Array.isArray(v) && v.length > 1000) {
                if (this.trickleTimer)
                    clearInterval(this.trickleTimer)

                this.trickleTimer = trickleCopy(v, this.subscribers as Subscriber<any[]>[])
            } else {
                run(v)
            }
        })
    }

    refresh() {
        fetch(this.url, options)
            .then(response => {
                logDebug(response)
                return response.json()
            })
            .then(this.dataTransformer)
            .then(data => {
                this.initialized = true
                this.objects.set(data);
            }).catch(error => {
            logError(error);
            return [];
        });
    }
}