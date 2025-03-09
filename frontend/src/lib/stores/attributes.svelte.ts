import type {Attribute} from "$lib/types/Attribute";


export const AttributesStore = $state<{ value: Attribute[] | undefined }>({value: undefined})

fetch("/api/rest/v1/attributes")
    .then((response) => response.json())
    .then((data) => {
        AttributesStore.value = data
    })

