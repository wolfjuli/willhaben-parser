import type {Listing} from "$lib/types/listing";

export const EmptyListing: Listing = {
    id: 0,
    willhabenId: 0,
    points: 0,
    lastSeen: new Date(),
    base: {},
    custom: {},
    user: {}
};