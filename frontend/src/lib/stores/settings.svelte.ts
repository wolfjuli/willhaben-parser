import type {Settings} from "$lib/types/Settings";
import {browser} from "$app/environment";


const defaultValues: Settings = {
    listingFields: [
        "mmo",
        "link",
        "points",
        "propertyType",
        "district",
        "address",
        "priceForDisplay",
        "estateSize",
        "price/m2",
        "willhabenId",
        "notes"
    ],
    searchFields: [
        "heading",
        "willhabenId"
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
