import {BaseRequestStoreStore} from "./BaseRequestStore";
import {attributesURL,  serverURL} from "../modules/config";
import type {Attribute} from "../types/Attribute";

class Attributes extends BaseRequestStoreStore<Attribute[]> {

    constructor() {
        super([], serverURL + attributesURL);
    }


    protected dataTransformer(data: any): Attribute[] {
        return data['data']
    }
}

export const attributes = new Attributes()