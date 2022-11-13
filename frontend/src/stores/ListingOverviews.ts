import {BaseRequestStoreStore} from "./BaseRequestStore";
import {listingOverviewsURL, listingsURL, serverURL} from "../modules/config";
import type {ListingOverview} from "../types/ListingOverview";

class ListingOverviews extends BaseRequestStoreStore<ListingOverview[]> {

    constructor() {
        super([], serverURL + listingOverviewsURL);
    }


    protected dataTransformer(data: any): ListingOverview[] {
        return data['data']
    }


}

export const listingOverviews = new ListingOverviews()