import {WithLocalStore} from "$lib/stores/WithLocalStore.svelte";
import type {RawSorting, SortingStoreType} from "$lib/types/Sorting";
import type {SearchParams} from "$lib/types/SearchParams";
import {FetchingStore} from "$lib/stores/FetchingStore.svelte";
import {ListingsStore} from "$lib/stores/ListingsStore.svelte";
import {Socket} from "$lib/utils/Socket";


export class SortingStore extends WithLocalStore<SortingStoreType> {
    static #instance: SortingStore

    private constructor() {
        super("sortingStore", () => ({
                sorting: [],
                lastUpdate: new Date('2020-01-01'),
            })
        );
    }

    static get instance(): SortingStore {
        if (!this.#instance)
            this.#instance = new SortingStore()

        return this.#instance
    }

    static get value(): SortingStoreType {
        return SortingStore.instance.value
    }


    static upsert(sorting: RawSorting[]): SortingStoreType {
        const newIds = sorting.map(s => s.listingId)
        this.value.lastUpdate = new Date()
        this.value.sorting = [...newIds]

        return this.value
    }

    fetch(searchParams: SearchParams) {
        Socket.send("getSorting", searchParams)
    }
}
