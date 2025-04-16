import type {Attribute} from "$lib/types/Attribute";
import type {Configuration} from "$lib/types/Configuration";

export interface ListingTableProps {
    sorting: number[],
    fields: Attribute[]
    attributes: Attribute[]
    configuration: Configuration
}
