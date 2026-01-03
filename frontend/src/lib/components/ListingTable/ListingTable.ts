import type {Configuration} from "$lib/types/Configuration";
import type {Listing} from "$lib/types/listing";

export type ListingTableProps = {
    configuration: Configuration,
    listings: Listing[],

}