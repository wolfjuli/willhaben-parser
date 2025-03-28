import type {Listing, UserListingMap} from "$lib/types/Listing";
import type {Attribute} from "$lib/types/Attribute";

export type ListingFilterProps = {
    userListings: UserListingMap
    listings: Listing[]
    attributes: Attribute[]
    onchange: (fun: (listing: Listing) => boolean) => void
}
