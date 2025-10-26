import {type Listing, type ListingsStoreType, type RawListing,} from '../types/Listing';
import {WithState} from "$lib/stores/WithState.svelte";

export class ListingsStore extends WithState<ListingsStoreType> {
    static #instance: ListingsStore

    private constructor() {
        super( ({
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

    static upsert(listings: RawListing[]): Listing[] {
        let newListings = {} as ListingsStoreType['listings']
        let newKnown = {} as ListingsStoreType['knownListings']
        listings.forEach(l => {
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

        return ListingsStore.partial(listings.map(l => l.listing.id))()
    }

    private knownListings(listingIds: number[]): string[] {
        return listingIds.map(lId => this.value.knownListings?.[lId])?.filter(Boolean)
    }
}
