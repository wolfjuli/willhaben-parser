import type {Listing} from "$lib/types/listing";

export interface ListingSearchProps {
    sorted: number[],
    onselect: (sel: Listing | undefined) => void
}
