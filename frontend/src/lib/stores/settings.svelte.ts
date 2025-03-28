import type {Settings} from "$lib/types/Settings";
import {browser} from "$app/environment";


const defaultValues: Settings = {
    listingFields: [
        "mmo",
        "heading",
        "points",
        "propertyType",
        "points",
        "district",
        "address",
        "priceForDisplay",
        "isPrivate",
        "estateSize",
        "livingArea"
    ]
}

export const settingsStore = $state<{ value: Settings }>({value: defaultValues});


if (browser) {
    const stored = localStorage.getItem("settings")

    if (!stored)
        setSettings(defaultValues)
    else
        setSettings(JSON.parse(stored))
}

export function setSettings(settings: Settings) {
    localStorage.setItem("settings", JSON.stringify(settings))
    settingsStore.value = settings
}
