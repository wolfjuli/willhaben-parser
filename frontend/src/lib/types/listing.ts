import type {SearchParams} from "$lib/types/SearchParams";

export type RawListing = {
    listing: Listing,
    md5: string
}
export type RawSorting = {
    listingId: number,
    points: number
}

export type Listing = {
    id: number,
    willhabenId: number,
    points: number
} & {
    [key: string]: {
        base: string | number,
        custom: string | number,
        user: string | number
    }
}

export type ListingsStoreMap = { [id: number]: Listing }

export type ListingsStoreType = {
    fetching: boolean,
    listings: ListingsStoreMap,
    knownMd5: { [id: number]: string },
    sorting: number[],
    lastUpdate: Date,
    searchParams: SearchParams,
    fetch: (listingId: number | undefined) => void,
    createListingValue: (listingValue: NewListingValue) => void,
    updateListingValue: (listingValue: NewListingValue) => void,
    deleteListingValue: (listingValue: NewListingValue) => void,
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
