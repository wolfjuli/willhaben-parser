import type {ListingsStoreMap} from "$lib/types/Listing";
import type {Attribute} from "$lib/types/Attribute";
import type {Configuration} from "$lib/types/Configuration";

export interface ListingTableProps {
    listings: ListingsStoreMap
    sorting: number[],
    fields: Attribute[]
    attributes: Attribute[]
    configuration: Configuration
}
