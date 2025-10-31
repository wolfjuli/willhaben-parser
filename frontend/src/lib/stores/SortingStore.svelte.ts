import {WithLocalStore} from "$lib/stores/WithLocalStore.svelte";
import type {RawSorting, SortingStoreType} from "$lib/types/Sorting";
import type {SearchParams} from "$lib/types/SearchParams";
import {Socket} from "$lib/api/Socket";


export class SortingStore extends WithLocalStore<SortingStoreType> {
    static #instance: SortingStore

    private constructor() {
        super("sortingStore", () => ({
                sorting: [],
                lastUpdate: new Date('2020-01-01'),
            })
        );
    }

    private static get instance(): SortingStore {
        if (!this.#instance)
            this.#instance = new SortingStore()

        return this.#instance
    }

    static get value(): SortingStoreType {
        return SortingStore.instance.value
    }


    static upsert(sorting: RawSorting[]): SortingStoreType {
        const newIds = sorting.map(s => s.listingId)
        SortingStore.value.lastUpdate = new Date()
        SortingStore.value.sorting = [...newIds]

        return SortingStore.value
    }

    static fetch(searchParams: SearchParams): Promise<SortingStoreType> {
        return new Promise<SortingStoreType>(async (resolve, reject) => {
            Socket.send("getSorting", searchParams, (data: RawSorting[]) => {
                resolve(SortingStore.upsert(data))
            })
        })
    }
}
