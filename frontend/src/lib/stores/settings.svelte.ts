import type {Settings} from "$lib/types/Settings";
import {browser} from "$app/environment";


const defaultValues: Settings = {
    "listingFields": [
        "attributeMap.mmo",
        "attributeMap.link",
        "seoUrl",
        "points",
        "attributeMap.propertyType",
        "attributeMap.district",
        "attributeMap.address",
        "attributeMap.coordinates",
        "attributeMap.priceForDisplay",
        "attributeMap.estateSize",
        "pricePerArea",
        "id",
        "notes"
    ],
    "searchFields": [
        "description",
        "id"
    ]
}

export const settingsStore = $state<{ value: Settings }>({value: defaultValues});


if (browser) {
    const stored = {...defaultValues, ...JSON.parse(localStorage.getItem("settings") ?? '{}')}
    setSettings(stored)
}

export function setSettings(settings: Settings) {
    localStorage.setItem("settings", JSON.stringify(settings))
    settingsStore.value = settings
}
