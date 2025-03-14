import type {Listing} from "$lib/types/Listing";
import type {Attribute} from "$lib/types/Attribute";
import type {Configuration} from "$lib/types/Configuration";

export interface ListingDetailProps {
    listing: Listing
    attributes: Attribute[]
    configuration: Configuration | undefined
}
