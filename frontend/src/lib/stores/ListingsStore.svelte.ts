import {
    type ListingsStoreType,
    type NewListingValue,
    type RawListing,
    type RawSorting,
} from '../types/Listing';
import {WithLocalStore} from "$lib/stores/WithLocalStore.svelte";
import {untrack} from "svelte";
import {FetchingStore} from "$lib/stores/FetchingStore.svelte";
import {ListingDb} from "$lib/stores/ListingsDb.svelte";


export class ListingsStore extends WithLocalStore<ListingsStoreType> {
    static #instance: ListingsStore

    private constructor() {
        super("listingsStore", () => ({
            sorting: [],
            lastUpdate: new Date('2020-01-01'),
            searchParams: {
                viewAttributes: [],
                searchString: "",
                searchAttributes: [],
                sortCol: "points",
                sortDir: "DESC"
            }
        }));

        if (new Date().valueOf() - new Date(this.value.lastUpdate).valueOf() > 10000)
            this.fetchSorting();

        let timer: any = 0
        $effect(() => {
            if (this.value.searchParams.searchString || true) {
                if (timer)
                    clearTimeout(timer)

                timer = setTimeout(() => {
                    untrack(() => this.fetchSorting())
                    clearTimeout(timer)
                    timer = 0
                }, 500)
            }
        })
    }

    static get instance(): ListingsStore {
        if (!this.#instance)
            this.#instance = new ListingsStore()

        return this.#instance
    }

    static get value(): ListingsStoreType {
        return ListingsStore.instance.value
    }

    fetchSorting() {
        FetchingStore.whileFetching("fetchSorting", () => {
                const params = this.value.searchParams
                const attrs = [...new Set([...params.searchAttributes, ...params.viewAttributes])].join(",")
                fetch(`/api/rest/v1/listings/sorting?sortCol=${params.sortCol}&sortDir=${params.sortDir}&searchString=${params.searchString}&searchAttrs=${attrs}`)
                    .then(r => r.json())
                    .then((sorting: RawSorting[]) => {
                        untrack(() => this.value.lastUpdate = new Date())
                        untrack(() => this.value.sorting = sorting.map(s => s.listingId))
                    })
            }
        )
    }

    fetch(listingIds: number[]): Promise<void> {
        return FetchingStore.whileFetching("fetchListing", () => {
            return untrack(() =>
                ListingDb.known(listingIds)
                    .then(async knownMd5 =>
                        await fetch(`/api/rest/v1/listings/full?ids=${listingIds.join(",")}&knownMd5=${knownMd5.join(",")}`)
                    )
                    .then(r => r.json())
                    .then(async (full: RawListing[]) => {
                            this.value.lastUpdate = new Date()
                            await ListingDb.addAll(full.map(f => f.listing))
                            await ListingDb.addAll(full.map(f => ({id: f.listing.id, md5: f.md5})))

                        }
                    )
            )
        })
    }

    static createListingValue = (listingValue: NewListingValue) =>
        FetchingStore.whileFetching("createListingValue", () =>
            fetch("/api/rest/v1/user_defined_attributes", {
                method: 'post',
                body: JSON.stringify(listingValue)
            })
                .then(() => ListingsStore.instance.fetch([listingValue.listingId]))
        )

    static updateListingValue = (listingValue: NewListingValue) =>
        FetchingStore.whileFetching("updateListingValue", () =>
            fetch("/api/rest/v1/user_defined_attributes", {
                method: 'put',
                body: JSON.stringify(listingValue)
            })
                .then(() => ListingsStore.instance.fetch([listingValue.listingId]))
        )

    static deleteListingValue = (listingValue: NewListingValue) =>
        FetchingStore.whileFetching("deleteListingValue", () =>
            fetch("/api/rest/v1/user_defined_attributes", {
                method: 'delete',
                body: JSON.stringify(listingValue)
            })
                .then(() => ListingsStore.instance.fetch([listingValue.listingId]))
        )
}
