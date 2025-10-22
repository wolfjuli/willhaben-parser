
export class FetchingStore {

    static #instance: FetchingStore
    fetchingStores: { [store: string]: number } = $state({})
    value = $derived(Object.values(FetchingStore.instance.fetchingStores).reduce((a, c) => a + c, 0))

    static get fetching(): number {
        return FetchingStore.instance.value
    }

    static get instance(): FetchingStore {
        if (!FetchingStore.#instance)
            FetchingStore.#instance = new FetchingStore()

        return FetchingStore.#instance
    }

    static whileFetching<T, R extends (void | Promise<T>)>(store: string, block: () => R): R {
        FetchingStore.startFetching(store)
        let ret: R
        try {
            ret = block()
        } finally {
            if (ret)
                ret.finally(() => FetchingStore.finishFetching(store))
            else
                FetchingStore.finishFetching(store)
        }

        return ret
    }

    static startFetching(store: string) {
        FetchingStore.instance.fetchingStores[store] = (FetchingStore.instance.fetchingStores[store] ?? 0) + 1
    }

    static finishFetching(store: string) {
        if (FetchingStore.instance.fetchingStores[store] && FetchingStore.instance.fetchingStores[store] === 1)
            delete FetchingStore.instance.fetchingStores[store];
    }

}
