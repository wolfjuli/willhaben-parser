import {BaseAttributesStore} from "$lib/stores/Attributes.svelte";
import type {BaseAttribute} from "$lib/types/Attribute";

export async function handleWSMessage(event: WebSocketEventMap['message']) {
    let body: { type: string, data: unknown } | { type: 'error' } = JSON.parse(event.data);

    switch (body.type) {
        case 'error':
            console.error(body)
            break;
        case 'pong':
            console.log(body);
            break;
        case 'attributes':
            BaseAttributesStore.upsert(body.data as BaseAttribute[]);
            break;


        default:
            console.log("Cannot handle message ", body)
    }
}
