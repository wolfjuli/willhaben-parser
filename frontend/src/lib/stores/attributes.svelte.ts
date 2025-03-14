import type {Attribute} from "$lib/types/Attribute";


export const AttributesStore = $state<{ value: Attribute[] | undefined }>({value: undefined})

function transform(a: Attribute) {
    return {...a, label: a.label ?? a.normalized}
}

fetch("/api/rest/v1/attributes")
    .then((response) => response.json())
    .then((data) => {
        AttributesStore.value = data.map(transform)
    })

export function filteredAttributes(normalized: string[]) {
    return normalized.map(f => AttributesStore.value?.find(a => a.normalized === f)!!).filter(Boolean)
}

