import type {Listing} from "../types/Listing";
import {BaseVolatileStore} from "./BaseVolatileStore";
import {BaseRequestStoreStore} from "./BaseRequestStore";
import {listingsURL, serverURL} from "../modules/config";

class Listings extends BaseRequestStoreStore<Listing[]> {

    constructor() {
        super([], serverURL + listingsURL);
    }


    protected dataTransformer(data: any): Listing[] {
        return data['data']
    }


}

export const listings = new Listings()