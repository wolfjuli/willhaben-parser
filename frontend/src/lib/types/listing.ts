import type {SearchParams} from "$lib/types/SearchParams";

export type RawListing = {
    listing: Listing,
    md5: string
}

export type RawSorting = {
    listingId: number,
    md5: string,
    points: number
}

export type Listing = {
    id: number,
    willhabenId: number,
    points: number
} & {
    base: { [key: string]: string | number }
    custom: { [key: string]: string | number }
    user: { [key: string]: string | number }
}

export type ListingsStoreType = {
    sorting: number[],
    lastUpdate: Date,
    searchParams: SearchParams
}

export type UserListing = {
    listingId: number,
    willhabenId: number,
    [key: string]: string | number
}

export type UserListingMap = {
    [key: number]: UserListing
}

export type NewListingValue = {
    listingId: number,
    attributeId: number,
    values: string[]
}
