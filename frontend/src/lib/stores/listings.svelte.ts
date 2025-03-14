import {type Listing, type RawListing} from '../types/Listing';
import {objectKeys, using} from "$lib/utils/object";


function transform(data: RawListing[]) {
    const numberFields = ["price", "estateSize", "postcode", "published", "livingArea", "priceSuggestion", "freeAreaAreaTotal"]
    const booleanFields = ["isprivate", "isBumped", "propertyTypeFlat"]

    const isNumberField = (field: string): boolean => numberFields.indexOf(field) > -1
    const isBooleanField = (field: string): boolean => booleanFields.indexOf(field) > -1

    const convertValue = (field: string, val: string): string | boolean | number => {
        if (isNumberField(field))
            return using(+val, (v) => isNaN(v) ? -1 : v)
        else if (isBooleanField(field))
            return using(val, (v) => val === "false" || val === "0" ? false : !!val)
        else
            return val
    }

    return data.map(d => ({
        willhabenId: d.willhabenId,
        points: Math.round(d.points * 100) / 100,
        ...objectKeys(d.attributes).reduce((acc, curr) => {
            acc[curr] = convertValue(curr.toString(), d.attributes[curr][0])
            return acc
        }, {} as { [key: string]: any })
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


