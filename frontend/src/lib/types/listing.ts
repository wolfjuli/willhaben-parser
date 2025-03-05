export type RawListing = {
    willhabenId: number,
    attributes: { [key: string]: any[] }
}

export type Listing = {
    willhabenId: number,
    [key: string]: string | number
}
