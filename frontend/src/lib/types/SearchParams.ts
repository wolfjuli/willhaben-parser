import type {Listing} from "$lib/types/Listing";

export type SearchParams = {
    viewAttributes: string[],
    searchString: string,
    searchAttributes: string[],
    sortCol: string,
    sortDir: string,
}
