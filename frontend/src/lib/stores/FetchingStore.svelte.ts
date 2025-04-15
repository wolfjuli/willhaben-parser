import {WithState} from "$lib/stores/WithState.svelte";

export class FetchingStore extends WithState<boolean> {

    static #instance: FetchingStore
    private fetchingStores: { [store: string]: number } = {}

    static get fetching(): boolean {
        return FetchingStore.instance.value
    }

    private static get instance(): FetchingStore {
        if (!FetchingStore.#instance)
            FetchingStore.#instance = new FetchingStore()

        return FetchingStore.#instance
    }

    static whileFetching<T>(store: string, block: () => void | Promise<T>) {
        FetchingStore.startFetching(store)
        let ret: Promise<T> | void = undefined
        try {
            ret = block()
        } finally {
            if (ret)
                ret.finally(() => FetchingStore.finishFetching(store))
            else
                FetchingStore.finishFetching(store)
        }
    }

    static startFetching(store: string) {
        FetchingStore.instance.fetchingStores[store] = (FetchingStore.instance.fetchingStores[store] ?? 0) + 1
    }

    static finishFetching(store: string) {
        if (FetchingStore.instance.fetchingStores[store] && FetchingStore.instance.fetchingStores[store]-- <= 0)
            delete FetchingStore.instance.fetchingStores[store];
    }
}
