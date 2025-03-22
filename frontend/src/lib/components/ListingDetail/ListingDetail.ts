import type {Listing, UserListing} from "$lib/types/Listing";
import type {Attribute} from "$lib/types/Attribute";
import type {Configuration} from "$lib/types/Configuration";

export interface ListingDetailProps {
    listing: Listing
    userListing: UserListing
    attributes: Attribute[]
    configuration: Configuration,
    horizontal: boolean
}
