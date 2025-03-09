import type {Listing} from "$lib/types/Listing";

export interface ListingSearchProps {
    listings: Listing[]
    onselect: (sel: Listing | undefined) => void
}
