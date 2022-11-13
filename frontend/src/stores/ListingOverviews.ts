import type {Listing} from "../types/Listing";
import {BaseVolatileStore} from "./BaseVolatileStore";
import {BaseRequestStoreStore} from "./BaseRequestStore";
import {listingOverviewsURL, listingsURL, serverURL} from "../modules/config";

class ListingOverviews extends BaseRequestStoreStore<Listing[]> {

    constructor() {
        super([], serverURL + listingOverviewsURL);
    }


    protected dataTransformer(data: any): Listing[] {
        return data['data']
    }


}

export const listingOverviews = new ListingOverviews()