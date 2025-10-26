import {BaseAttributesStore} from "$lib/stores/Attributes.svelte";
import type {BaseAttribute} from "$lib/types/Attribute";
import {ListingsStore} from "$lib/stores/ListingsStore.svelte";
import type {ListingsStoreType, RawListing} from "$lib/types/listing";
import {SortingStore} from "$lib/stores/SortingStore.svelte";

export async function handleWSMessage(event: WebSocketEventMap['message']) {
    let body: { type: string, data: unknown } | { type: 'error' } = JSON.parse(event.data);

    switch (body.type) {
        case 'error':
            console.error(body)
            break;
        case 'pong':
            console.log(body);
            break;
        case 'getAttributes':
            BaseAttributesStore.upsert(body.data as BaseAttribute[]);
            break;
        case 'getListings':
            ListingsStore.upsert(body.data as RawListing[]);
            break;
        case 'getSorting':
            SortingStore.upsert(body.data as RawListing[]);
            break;

        default:
            console.log("Cannot handle message ", body)
    }
}
