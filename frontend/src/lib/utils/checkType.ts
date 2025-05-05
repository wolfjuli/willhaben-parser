import type {Listing} from "$lib/types/listing";

export function isListing(l: object | undefined): l is Listing {
    if (!l) return false

    const t = l as Listing

    return t.id !== undefined && t.willhabenId !== undefined
}

export function isKnownListing(l: object | undefined): l is { listing: Listing, md5: string } {
    if (!l || !l.listing || !l.md5) return false

    const t = l.listing as Listing

    return t.id !== undefined && t.willhabenId !== undefined
}

