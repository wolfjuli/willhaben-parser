import type {Listing} from "$lib/types/Listing";

export function listingFilter(searchTerm: string | undefined): (l: Listing) => boolean {
    return (l: Listing): boolean => {
        return searchTerm ? l.willhabenId.toString().includes(searchTerm.toLocaleLowerCase()) ||
            (l.price?.toString()?.includes(searchTerm) ?? false) ||
            (l.points?.toString()?.includes(searchTerm) ?? false) ||
            (l.heading?.toString()?.toLocaleLowerCase().includes(searchTerm.toLocaleLowerCase()) ?? false) ||
            (l.bodyDyn?.toString()?.toLowerCase().includes(searchTerm.toLocaleLowerCase()) ?? false)
            : true
    }
}
