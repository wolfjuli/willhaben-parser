import {WithLocalStore} from "$lib/stores/WithLocalStore.svelte";
import type {SearchParams} from "$lib/types/SearchParams";


export class SearchParamsStore extends WithLocalStore<SearchParams> {
    static #instance: SearchParamsStore

    private constructor() {
        super("searchParamsStore", () => ({
                viewAttributes: [],
                searchString: "",
                searchAttributes: [],
                sortCol: "points",
                sortDir: "DESC"
            })
        );
    }

    static get instance(): SearchParamsStore {
        if (!this.#instance)
            this.#instance = new SearchParamsStore()

        return this.#instance
    }

    static get value(): SearchParams {
        return SearchParamsStore.instance.value
    }
}
