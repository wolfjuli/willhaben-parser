import type {Attribute} from "$lib/types/Attribute"
import type {Listing, UserListing} from "$lib/types/Listing"
import type {Configuration} from "$lib/types/Configuration";

export type ListingValueProps = {
    listing: Listing
    userListing: UserListing | undefined
    attribute: Attribute
    configuration: Configuration
    onclick: (val: object | string | number, listing: Listing) => void
    ondblclick: (val: object | string | number, listing: Listing) => void
}

export type EditListingValueProps = {
    listing: Listing
    userListing: UserListing | undefined
    attribute: Attribute
    oncreate: (val: string, listing: Listing, attribute: Attribute) => void
    onupdate: (val: string, listing: Listing, attribute: Attribute) => void
}
