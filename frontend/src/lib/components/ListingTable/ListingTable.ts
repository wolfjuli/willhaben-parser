import type {Listing, UserListingMap} from "$lib/types/Listing";
import type {Attribute} from "$lib/types/Attribute";
import type {Configuration} from "$lib/types/Configuration";
import type {FunctionDefMap} from "$lib/stores/functions.svelte";

export interface ListingTableProps {
    listings: Listing[]
    userListings: UserListingMap
    fields: Attribute[]
    attributes: Attribute[]
    configuration: Configuration
    functions: FunctionDefMap
}
