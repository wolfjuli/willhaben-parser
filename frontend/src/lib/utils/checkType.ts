import type {Listing} from "$lib/types/listing";

export function isListing(l: object | undefined): l is Listing {
    if (!l) return false

    const t = l as Listing

    return t.id !== undefined && t.willhabenId !== undefined
}


