import {WithLocalStore} from "$lib/stores/WithLocalStore.svelte";
import type {SearchParams} from "$lib/types/SearchParams";
import type {RawListing} from "$lib/types/listing";


export class SearchParamsStore extends WithLocalStore<SearchParams> {
    static #instance: SearchParamsStore

    private constructor() {
        super("searchParamsStore", () => ({
                viewAttributes: [],
                searchString: "",
                searchAttributes: [],
                sortCol: "points",
                sortDir: "DESC",
                page: 1,
            })
        );
    }

    static upsert(listings: RawListing[]): RawListing[] {
        return listings
    }

    static get instance(): SearchParamsStore {
        if (!this.#instance)
            this.#instance = new SearchParamsStore()

        return this.#instance
    }

    static get value(): SearchParams {
        return SearchParamsStore.instance.value
    }

    static set(value: SearchParams): SearchParams {
        console.log("SearchParamsStore set value", value)
        SearchParamsStore.instance.value = value
        return value
    }
}
