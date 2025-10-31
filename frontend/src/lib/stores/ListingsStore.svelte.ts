import {type Listing, type ListingsStoreType, type RawListing,} from '$lib/types/listing';
import {WithState} from "$lib/stores/WithState.svelte";
import {Socket} from "$lib/api/Socket";
import type {CreateUserAttribute} from "$lib/types/Attribute";

export class ListingsStore extends WithState<ListingsStoreType> {
    static #instance: ListingsStore

    private constructor() {
        super(({
            lastUpdate: new Date('2020-01-01'),
            listings: {},
            knownListings: {}
        }));

        Socket.register("getListings", ListingsStore.upsert)
        Socket.register("setListingUserAttribute", (it: RawListing) => ListingsStore.upsert([it]))
    }

    static get instance(): ListingsStore {
        if (!this.#instance)
            this.#instance = new ListingsStore()

        return this.#instance
    }

    static get value(): ListingsStoreType {
        return ListingsStore.instance.value
    }

    static partial(listingIds: number[]): () => Listing[] {
        // @ts-ignore
        return () => (listingIds.map(id => ListingsStore.value.listings?.[id])?.filter(Boolean) ?? [])
    }

    static get(listingId: number): Listing | undefined {
        return ListingsStore.instance.value.listings?.[listingId]
    }

    static upsert(listings: RawListing[]): Listing[] {
        listings.forEach(l => {
            ListingsStore.value.listings[l.listing.id] = l.listing
            ListingsStore.value.knownListings[l.listing.id] = l.md5
        })

        return ListingsStore.partial(listings.map(l => l.listing.id))()
    }

    static fetch(listingIds: number[], page: number | undefined = undefined): Promise<Listing[]> {
        const ids = page ? listingIds.slice((page - 1) * 100, page * 100) : listingIds
        return new Promise((resolve, reject) => {
            Socket.send("getListings", {ids}, (data: RawListing[]) => {
                resolve(ListingsStore.upsert(data))
            })
        })
    }

    static set(userAttribute: CreateUserAttribute): CreateUserAttribute {
        Socket.send("setListingUserAttribute", userAttribute)
        return userAttribute
    }

    private knownListings(listingIds: number[]): string[] {
        return listingIds.map(lId => this.value.knownListings?.[lId])?.filter(Boolean)
    }
}
