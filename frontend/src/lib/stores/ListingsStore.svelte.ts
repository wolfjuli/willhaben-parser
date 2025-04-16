import {
    type Listing,
    type ListingsStoreType,
    type NewListingValue,
    type RawListing,
    type RawSorting,
} from '../types/Listing';
import type {SearchParams} from "$lib/types/SearchParams";
import {WithLocalStore} from "$lib/stores/WithLocalStore.svelte";
import {untrack} from "svelte";
import {FetchingStore} from "$lib/stores/FetchingStore.svelte";


export class ListingsStore extends WithLocalStore<ListingsStoreType> {
    static #instance: ListingsStore
    private constructor() {
        super("listingsStore", () => ({
            listings: {},
            knownMd5: {},
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
            this.fetch();
    }

    filterFn: (l: Listing) => boolean = $derived((l: Listing) => {
        const searchTerm = this.value.searchParams.searchString.toLowerCase()
        const attrs = [...new Set([...this.value.searchParams.searchAttributes ,...this.value.searchParams.viewAttributes])]

        console.log("has search term")
            if(searchTerm) {
                return l.willhabenId.toString().indexOf(searchTerm) > -1 ||
                    attrs.some(a => {
                        const val = (l[a]?.user ?? l[a]?.custom ?? l[a]?.base ?? "").toString()
                        return val.toLowerCase().indexOf(searchTerm) > -1
                    })
            } else {
                return true
            }
    })

    filtered: number[] = $derived(this.value.sorting.filter(id => this.filterFn(this.value.listings[id])))



    static get instance(): ListingsStore {
        if (!this.#instance)
            untrack(() => {
                this.#instance = new ListingsStore()
            })

        return this.#instance
    }

    static get value(): ListingsStoreType {
        return ListingsStore.instance.value
    }

    fetchSorting() {
        FetchingStore.whileFetching("fetchSorting", () =>
            fetch(`/api/rest/v1/listings/sorting?sortCol=${this.value.searchParams.sortCol}&sortDir=${this.value.searchParams.sortDir}`)
                .then(r => r.json())
                .then((sorting: RawSorting[]) => this.value.sorting = sorting.map(s => s.listingId))
        )
    }

    fetch(listingId: number | undefined = undefined) {
        FetchingStore.whileFetching("fetchListing", () => {
            const filter = listingId ? `listingId=${listingId}` : `knownMd5=${Object.values(this.value.knownMd5).join(",")}`
            const sorting = `sortCol=${this.value.searchParams.sortCol}&sortDir=${this.value.searchParams.sortDir}`
            const lastUpdate = new Date()

            return Promise.all([
                fetch(`/api/rest/v1/listings/full?${filter}`).then(r => r.json()),
                fetch(`/api/rest/v1/listings/sorting?${sorting}`).then(r => r.json()),
            ]).then(([full, sorting]: [RawListing[], RawSorting[]]) => {
                    const sortMap = sorting.reduce((acc, s) => {
                        acc[s.listingId] = s.points;
                        return acc
                    }, {} as Record<number, number>)
                    full.forEach(d => {
                        this.value.listings[d.listing.id] = {...d.listing, points: sortMap[d.listing.id] ?? 0} as Listing
                        this.value.knownMd5[d.listing.id] = d.md5
                        this.value.lastUpdate = lastUpdate
                    })

                    this.value.sorting = sorting.map(s => s.listingId)
                }
            )
        })
    }

    static createListingValue = (listingValue: NewListingValue) =>
        fetch("/api/rest/v1/user_defined_attributes", {
            method: 'post',
            body: JSON.stringify(listingValue)
        })
            .then(() => ListingsStore.instance.fetch(listingValue.listingId))

    static updateListingValue = (listingValue: NewListingValue) =>
        fetch("/api/rest/v1/user_defined_attributes", {
            method: 'put',
            body: JSON.stringify(listingValue)
        })
            .then(() => ListingsStore.instance.fetch(listingValue.listingId))

    static deleteListingValue = (listingValue: NewListingValue) =>
        fetch("/api/rest/v1/user_defined_attributes", {
            method: 'delete',
            body: JSON.stringify(listingValue)
        })
            .then(() => ListingsStore.instance.fetch(listingValue.listingId))
}
