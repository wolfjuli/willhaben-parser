export type RawListing = {
    willhabenId: number,
    points: number,
    attributes: { [key: string]: any[] }
}

export type Listing = {
    willhabenId: number,
    points: number,
    [key: string]: string | number
}
