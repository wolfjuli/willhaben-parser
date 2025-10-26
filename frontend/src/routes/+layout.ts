import {SearchParamsStore} from "$lib/stores/SearchParamsStore.svelte";
import {SortingStore} from "$lib/stores/SortingStore.svelte";
import {ListingsStore} from "$lib/stores/ListingsStore.svelte";
import {ScriptsStore} from "$lib/stores/ScriptsStore.svelte";
import {BaseAttributesStore} from "$lib/stores/Attributes.svelte";
import {initializer} from "$lib/stores/initializeStores.svelte";

export const ssr = false

export const load = async (event) => {
    // const configuration = await fetch("/api/rest/v1/configuration")
    //     .then((response) => response.json())
    //     .then((data) => data[0])


    return {
        configuration: {}
    }
}
