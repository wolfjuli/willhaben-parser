import type {Listing} from "$lib/types/Listing";
import type {BaseAttribute} from "$lib/types/Attribute";
import type {Configuration} from "$lib/types/Configuration";
import type {FunctionDefMap} from "$lib/stores/functions.svelte";

export interface ListingDetailProps {
    listing: Listing
    attributes: BaseAttribute[]
    configuration: Configuration | undefined
    functions: FunctionDefMap
}
