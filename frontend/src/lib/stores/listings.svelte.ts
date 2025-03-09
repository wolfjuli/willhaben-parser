import {type Listing, type RawListing} from '../types/Listing';
import {objectKeys, using} from "$lib/utils/object";


function transform(data: RawListing[]) {
    const numberFields = ["PRICE", "ESTATE_SIZE", "POSTCODE", "PUBLISHED", "ESTATE_SIZE/LIVING_AREA", "ESTATE_PRICE/PRICE_SUGGESTION", "FREE_AREA/FREE_AREA_AREA_TOTAL"]
    const booleanFields = ["ISPRIVATE", "IS_BUMPED", "PROPERTY_TYPE_FLAT"]
    const fieldMapping = {
        "FREE_AREA/FREE_AREA_AREA_TOTAL": "FREE_AREA_AREA_TOTAL",
        "ESTATE_PRICE/PRICE_SUGGESTION": "PRICE_SUGGESTION",
        "ESTATE_SIZE/LIVING_AREA": "ESTATE_SIZE_LIVING_AREA"
    }

    const isNumberField = (field: string | number): boolean => numberFields.indexOf(field) > -1
    const isBooleanField = (field: string | number): boolean => booleanFields.indexOf(field) > -1

    const convertValue = (field: string | number, val: string): string | boolean | number => {
        if (isNumberField(field))
            return using(+val, (v) => isNaN(v) ? -1 : v)
        else if (isBooleanField(field))
            return using(val, (v) => val === "false" || val === "0" ? false : !!val)
        else
            return val
    }

    return data.map(d => ({
        willhabenId: d.willhabenId,
        ...objectKeys(d.attributes).reduce((acc, curr) => {
            acc[fieldMapping[curr] ?? curr] = convertValue(curr, d.attributes[curr][0])
            return acc
        }, {})
    }))

}

export const ListingsStore = $state<{ value: Listing[] | undefined }>({value: undefined})

fetch("/api/rest/v1/fe_listings")
    .then((response) => {
        return response.json()
    })
    .then((data) => transform(data))
    .then((data) => {
        ListingsStore.value = data;
    })


