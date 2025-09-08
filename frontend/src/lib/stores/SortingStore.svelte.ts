import {WithLocalStore} from "$lib/stores/WithLocalStore.svelte";
import type {RawSorting, SortingStoreType} from "$lib/types/Sorting";
import type {SearchParams} from "$lib/types/SearchParams";
import {FetchingStore} from "$lib/stores/FetchingStore.svelte";
import {ListingsStore} from "$lib/stores/ListingsStore.svelte";


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

    fetch(searchParams: SearchParams): Promise<SortingStoreType> {
        return FetchingStore.whileFetching("fetchSorting", () => {
                const params = searchParams
                const attrs = [...new Set([...(params.searchAttributes ?? []), ...(params.viewAttributes ?? [])].join(","))]
                return fetch(`/api/rest/v1/listings/sorting?sortCol=${params.sortCol}&sortDir=${params.sortDir}&searchString=${params.searchString}&searchAttrs=${attrs}`)
                    .then(r => r.json())
                    .then(async (sorting: RawSorting[]) => {
                        const newIds = sorting.map(s => s.listingId)
                        this.value.lastUpdate = new Date()
                        this.value.sorting = [...newIds]

                        await ListingsStore.instance.fetch(newIds)
                        return this.value
                    })
            }
        )
    }
}
