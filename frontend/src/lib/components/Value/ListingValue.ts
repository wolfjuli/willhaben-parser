import type {Attribute} from "$lib/types/Attribute"
import type {Listing} from "$lib/types/Listing"
import type {Configuration} from "$lib/types/Configuration";

export type ListingValueProps = {
    listing: Listing
    attribute: Attribute
    configuration: Configuration
    onclick: (val: object | string | number, listing: Listing) => void
    ondblclick: (val: object | string | number, listing: Listing) => void
}

export type EditListingValueProps = {
    listing: Listing
    attribute: Attribute
    oncreate: (val: string, listing: Listing, attribute: Attribute) => void
    onupdate: (val: string, listing: Listing, attribute: Attribute) => void
}
