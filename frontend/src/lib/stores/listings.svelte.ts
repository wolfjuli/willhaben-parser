import {type Listing, type NewListingValue, type RawListing, type UserListingMap} from '../types/Listing';
import {objectKeys, using} from "$lib/utils/object";
import {toMap} from '$lib/utils/toMap'


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
        id: d.id,
        willhabenId: d.willhabenId,
        points: Math.round(d.points * 100) / 100,
        ...objectKeys(d.attributes).reduce((acc, curr) => {
            acc[curr] = convertValue(curr.toString(), d.attributes[curr][0])
            return acc
        }, {} as { [key: string]: any })
    }))
}

export const ListingFilter = $state<{ limit: number | null, page: number | null, searchTerm: string }>({
    limit: 100,
    page: 1,
    searchTerm: ""
})
export const ListingsStore = $state<{ value: Listing[] | undefined }>({value: undefined})
export const UserListingsStore = $state<{ value: UserListingMap }>({value: {}})

function updateListings() {
    fetch(`/api/rest/v1/fe_listings?limit=${ListingFilter.limit}&page=${ListingFilter.page}`)
        .then(r => r.json())
        .then((data) => transform(data))
        .then((data) => {
            ListingsStore.value = data;
        })

    fetch(`/api/rest/v1/fe_user_listings?limit=${ListingFilter.limit}&page=${ListingFilter.page}`)
        .then(r => r.json())
        .then((data) => transform(data))
        .then((data) => {
            UserListingsStore.value = data.reduce(toMap(o => o.willhabenId), {});
        })
}

updateListings()

export const createListingValue = (listingValue: NewListingValue) =>
    fetch("/api/rest/v1/user_defined_attributes", {
        method: 'post',
        body: JSON.stringify(listingValue)
    })
        .then(r => r.json())
        .then(sf => fetch(`/api/rest/v1/fe_user_listings?listingId=${sf.listingId}`))
        .then(r => r.json())
        .then((data) => transform(data))
        .then(uvs => {
            const uv = uvs[0]
            const existing = UserListingsStore.value!![uv.willhabenId]
            UserListingsStore.value!![uv.willhabenId] = {...existing, ...uv}
        })

export const updateListingValue = (listingValue: NewListingValue) =>
    fetch("/api/rest/v1/user_defined_attributes", {
        method: 'put',
        body: JSON.stringify(listingValue)
    })
        .then(r => r.json())
        .then(sf => fetch(`/api/rest/v1/fe_user_listings?listingId=${sf.listingId}`))
        .then(r => r.json())
        .then((data) => transform(data))
        .then(uvs => {
            const uv = uvs[0]
            const existing = UserListingsStore.value!![uv.willhabenId]
            UserListingsStore.value!![uv.willhabenId] = {...existing, ...uv}
        })

export const deleteListingValue = (listingValue: NewListingValue) =>
    fetch("/api/rest/v1/user_defined_attributes", {
        method: 'delete',
        body: JSON.stringify(listingValue)
    })
        .then(() => fetch(`/api/rest/v1/fe_user_listings?listingId=${listingValue.listingId}`))
        .then(r => r.json())
        .then((data) => transform(data))
        .then(uvs => {
            if (uvs?.[0]) {
                const uv = uvs[0]
                const existing = UserListingsStore.value!![uv.willhabenId]
                UserListingsStore.value!![uv.willhabenId] = {...existing, ...uv}
            } else {
                updateUserListings()
            }
        })

export const updateListingPoints = (): Promise<{ [key: string]: unknown }[]> =>
    fetch("/api/rest/v1/fe_update_listing_points", {
        method: 'get'
    })
        .then(r => {
            updateUserListings();
            return r
        })
        .then(r => r.json())

