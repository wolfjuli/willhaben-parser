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
import {ListingDb} from "$lib/stores/ListingsDb.svelte";


export class ListingsStore extends WithLocalStore<ListingsStoreType> {
    static #instance: ListingsStore

    private constructor() {
        super("listingsStore", () => ({
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

        $effect(() => {
            console.log("Update filtered")
             Promise.all(this.value.sorting
                .map(async id => this.filterFn(await ListingDb.get(id)) ? id : undefined))
                 .then(d => this.filtered = d.filter(f => f !== undefined))
        })
    }

    filterFn: (l: Listing) => boolean = $derived((l: Listing) => {
        const searchTerm = this.value.searchParams.searchString.toLowerCase()
        const attrs = [...new Set([...this.value.searchParams.searchAttributes, ...this.value.searchParams.viewAttributes])]

        if (searchTerm) {
            return l.willhabenId.toString().indexOf(searchTerm) > -1 ||
                attrs.some(a => {
                    const val = (l[a]?.user ?? l[a]?.custom ?? l[a]?.base ?? "").toString()
                    return val.toLowerCase().indexOf(searchTerm) > -1
                })
        } else {
            return true
        }
    })

    filtered: number[] = $state([])


    static get instance(): ListingsStore {
        if (!this.#instance)
            this.#instance = new ListingsStore()

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
            ]).then(async ([full, sorting]: [RawListing[], RawSorting[]]) => {
                    const sortMap = sorting.reduce((acc, s) => {
                        acc[s.listingId] = s.points;
                        return acc
                    }, {} as Record<number, number>)

                    await ListingDb.addAll(full.map ( d =>{
                        this.value.knownMd5[d.listing.id] = d.md5
                        this.value.lastUpdate = lastUpdate

                        return {...d.listing, points: sortMap[d.listing.id] ?? 0} as Listing
                    } ) as Listing[])

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
