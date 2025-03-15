import type {Attribute, BaseAttribute, CustomAttribute} from "$lib/types/Attribute";


export const BaseAttributesStore = $state<{ value: BaseAttribute[] | undefined }>({value: undefined})
export const CustomAttributesStore = $state<{ value: CustomAttribute[] | undefined }>({value: undefined})

function transform(a: BaseAttribute) {
    return {...a, label: a.label ?? a.normalized}
}

fetch("/api/rest/v1/attributes")
    .then(r => r.json())
    .then(d => BaseAttributesStore.value = d.map(transform))

fetch("/api/rest/v1/custom_attributes")
    .then(r => r.json())
    .then(d => CustomAttributesStore.value = d.map(transform))

export function mergedAttributes(): { value: (BaseAttribute | CustomAttribute)[] } {
    const custom = CustomAttributesStore.value?.map(a => a.normalized) ?? []
    const baseAttr = BaseAttributesStore.value?.filter(a => !custom.find(c => c === a.normalized)) ?? []
    return {value: [...baseAttr, ...(CustomAttributesStore.value ?? [])]}
}

export function filteredAttributes(normalized: string[]): Attribute[] {
    return normalized.map(f => mergedAttributes().value?.find(a => a.normalized === f)!!).filter(Boolean)
}

