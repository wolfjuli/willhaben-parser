import type {Listing} from "$lib/types/Listing";
import type {Attribute} from "$lib/types/Attribute";
import type {Configuration} from "$lib/types/Configuration";
import type {FunctionDefMap} from "$lib/stores/functions.svelte";

export interface ListingTableProps {
    listings: Listing[]
    fields: Attribute[]
    configuration: Configuration
    functions: FunctionDefMap
}
