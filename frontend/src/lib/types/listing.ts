


export type RawListing = {
    listing: Listing,
    md5: string
}

export type ListingAttribute = {
    base: string | number | undefined
    custom: string | number | undefined
    user: string | number | undefined
    root: string | number | undefined
}

export type Listing = {
    id: number,
    willhabenId: number,
    points: number,
    lastSeen: Date
} & {
    base: { [key: string]: string | number }
    custom: { [key: string]: string | number }
    user: { [key: string]: string | number }
}

export type ListingsStoreType = {
    lastUpdate: Date,
    listings: { [id: number]: Listing },
    knownListings: { [id: number]: string }
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
