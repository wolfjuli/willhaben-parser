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

export class ListingsStore extends WithLocalStore<ListingsStoreType> {
    private static instance: ListingsStore

    private constructor() {
        super("listingsStore", () => ({
            fetching: false,
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
            },
            fetch: (listingId: number | undefined = undefined) => this.fetch(listingId),
            createListingValue: (listingValue: NewListingValue) => this.createListingValue(listingValue),
            updateListingValue: (listingValue: NewListingValue) => this.updateListingValue(listingValue),
            deleteListingValue: (listingValue: NewListingValue) => this.deleteListingValue(listingValue),
        }));

        if (new Date().valueOf() - new Date(this.value.lastUpdate).valueOf() > 10000)
            this.fetch();
    }

    static get value(): ListingsStoreType {
        untrack(() => {
            if (!ListingsStore.instance)
                ListingsStore.instance = new ListingsStore()
        })

        return ListingsStore.instance.value
    }

    fetch(listingId: number | undefined = undefined) {
        this.value.fetching = true
        const filter = listingId ? `listingId=${listingId}` : `knownMd5=${Object.values(this.value.knownMd5).join(",")}`
        const sorting = `sortCol=${this.value.searchParams.sortCol}&sortDir=${this.value.searchParams.sortDir}`
        const lastUpdate = new Date()

        Promise.all([
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
        ).finally(() => {
            this.value.fetching = false
        })
    }

    createListingValue = (listingValue: NewListingValue) =>
        fetch("/api/rest/v1/user_defined_attributes", {
            method: 'post',
            body: JSON.stringify(listingValue)
        })
            .then(() => this.fetch(listingValue.listingId))

    updateListingValue = (listingValue: NewListingValue) =>
        fetch("/api/rest/v1/user_defined_attributes", {
            method: 'put',
            body: JSON.stringify(listingValue)
        })
            .then(() => this.fetch(listingValue.listingId))

    deleteListingValue = (listingValue: NewListingValue) =>
        fetch("/api/rest/v1/user_defined_attributes", {
            method: 'delete',
            body: JSON.stringify(listingValue)
        })
            .then(() => this.fetch(listingValue.listingId))
}

export const ListingSearchParams = $state<SearchParams>(
    {
        viewAttributes: [],
        searchString: "",
        searchAttributes: [],
        sortCol: "points",
        sortDir: "DESC"
    }
)
