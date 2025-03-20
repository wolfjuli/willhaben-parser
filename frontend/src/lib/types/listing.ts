export type RawListing = {
    id: number,
    willhabenId: number,
    points: number,
    attributes: { [key: string]: any[] }
}

export type Listing = {
    id: number,
    willhabenId: number,
    points: number,
    [key: string]: string | number
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
