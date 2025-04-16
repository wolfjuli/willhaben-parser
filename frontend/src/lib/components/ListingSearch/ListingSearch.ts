import type {Listing} from "$lib/types/Listing";

export interface ListingSearchProps {
    sorted: number[],
    onselect: (sel: Listing | undefined) => void
}
