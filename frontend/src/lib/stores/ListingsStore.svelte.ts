import {type Listing, type ListingsStoreType, type NewListingValue, type RawListing,} from '../types/Listing';
import {untrack} from "svelte";
import {FetchingStore} from "$lib/stores/FetchingStore.svelte";
import {WithState} from "$lib/stores/WithState.svelte";

export class ListingsStore extends WithState<ListingsStoreType> {
    static #instance: ListingsStore

    private constructor() {
        super(({
            lastUpdate: new Date('2020-01-01'),
            listings: {},
            knownListings: {}
        }));
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
        return () => (listingIds.map(id => ListingsStore.value.listings?.[id]?.find(Boolean))?.filter(Boolean) ?? [])
    }

    static get(listingId: number): Listing | undefined {
        return ListingsStore.instance.value.listings?.[listingId]?.find(Boolean)
    }


    static deleteListingValue = (listingValue: NewListingValue) =>
        FetchingStore.whileFetching("deleteListingValue", () =>
            fetch(`/api/rest/v1/listings/${listingValue.listingId}/${listingValue.attributeId}`, {
                method: 'delete',
                headers: {
                    "content-type": "application/json"
                },
            })
                .then(() => ListingsStore.instance.fetch([listingValue.listingId]))
        )

    fetch(listingIds: number[]): Promise<Listing[]> {
        return FetchingStore.whileFetching("fetchListing", async () =>
            untrack(async () =>
                await fetch(
                    `/api/rest/v1/listings`, {
                        method: 'post',
                        headers: {
                            "content-type": "application/json"
                        },
                        body: JSON.stringify({
                            knownMd5: this.knownListings(listingIds),
                            ids: listingIds
                        })
                    }
                )
                    .then(r => r.json())
                    .then(async (full: RawListing[]) => {

                        let newListings = {} as ListingsStoreType['listings']
                        let newKnown = {} as ListingsStoreType['knownListings']

                        full.forEach(l => {
                            newListings[l.listing.id] = [...(this.value.listings?.[l.listing.id]?.filter(ol => ol.lastSeen !== l.listing.lastSeen) ?? []),
                                {
                                    ...l.listing,
                                    points: l.listing.custom.points,
                                    willhabenId: l.listing.base.id
                                }
                            ].sort((a, b) => b.lastSeen.getDate() - a.lastSeen.getDate())

                            newKnown[l.listing.id] = l.md5
                        })

                        this.value.listings = {...this.value.listings, ...newListings}
                        this.value.knownListings = {...this.value.knownListings, ...newKnown}

                        return ListingsStore.partial(listingIds)()
                    })
            )
        )
    }

    static createListingValue = (listingValue: NewListingValue) =>
        FetchingStore.whileFetching("createListingValue", () =>
            fetch(`/api/rest/v1/listings/${listingValue.listingId}/${listingValue.attributeId}`, {
                method: 'post',
                headers: {
                    "content-type": "application/json"
                },
                body: JSON.stringify(listingValue.values)
            })
                .then(() => ListingsStore.instance.fetch([listingValue.listingId]))
        )

    static updateListingValue = (listingValue: NewListingValue) =>
        FetchingStore.whileFetching("updateListingValue", () =>
            fetch(`/api/rest/v1/listings/${listingValue.listingId}/${listingValue.attributeId}`, {
                method: 'post',
                headers: {
                    "content-type": "application/json"
                },
                body: JSON.stringify(listingValue.values)
            })
                .then(() => ListingsStore.instance.fetch([listingValue.listingId]))
        )

    private knownListings(listingIds: number[]): string[] {
        return listingIds.map(lId => this.value.knownListings?.[lId])?.filter(Boolean)
    }
}
