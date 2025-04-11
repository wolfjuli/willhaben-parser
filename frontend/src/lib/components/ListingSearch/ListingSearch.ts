import type {Listing, ListingsStoreMap} from "$lib/types/Listing";

export interface ListingSearchProps {
    listings: ListingsStoreMap
    sorted: number[],
    onselect: (sel: Listing | undefined) => void
}
