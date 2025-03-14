import type {Listing} from "$lib/types/Listing";
import type {Attribute} from "$lib/types/Attribute";
import type {Configuration} from "$lib/types/Configuration";

export interface ListingTableProps {
    listings: Listing[]
    fields: Attribute[]
    configuration: Configuration
}
