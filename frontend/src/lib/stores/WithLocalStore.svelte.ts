import {browser} from "$app/environment";
import {WithState} from "$lib/stores/WithState.svelte";

export class WithLocalStore<T> extends WithState<T> {
    key = '';

    constructor(key: string, initialValue: () => T) {
        super()
        this.key = key;

        if (browser) {
            const item = localStorage.getItem(key);
            this.value = item ? this.deserialize(item) : initialValue();
        } else {
            this.value = initialValue()
        }

        $effect(() => {
            localStorage.setItem(this.key, this.serialize(this.value));
        });
    }

    serialize(value: T): string {
        return JSON.stringify(value);
    }

    deserialize(item: string): T {
        return JSON.parse(item);
    }
}
