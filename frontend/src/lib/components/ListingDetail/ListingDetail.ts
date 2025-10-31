import type {Listing} from "$lib/types/listing";
import type {Attribute} from "$lib/types/Attribute";
import type {Configuration} from "$lib/types/Configuration";

export interface ListingDetailProps {
    listing: Listing
    attributes: Attribute[]
    configuration: Configuration,
    horizontal: boolean
}
