import type {Listing} from "$lib/types/Listing";
import type {Attribute} from "$lib/types/Attribute";
import type {FunctionDefMap} from "$lib/stores/functions.svelte";

export function transformListing(listing: Listing, attributes: Attribute[], functions: FunctionDefMap): Listing {
    return attributes.reduce((lst, attr) => {
        const fun: (val: string | number, row: Listing) => string = attr.functionId ? eval(functions[attr.functionId].function) : () => lst[attr.normalized]
        lst[attr.normalized] = fun(lst[attr.normalized], lst)
        return lst
    }, {...listing} as Listing)
}
