<script lang="ts">
    import {ListingSearchParams, ListingsStore, updateListings, UserListingsStore} from '$lib/stores/listings.svelte'
    import type {PageProps} from "./$types";
    import ListingTable from "$lib/components/ListingTable/ListingTable.svelte";
    import {filteredAttributes, mergedAttributes} from "$lib/stores/attributes.svelte";
    import {settingsStore} from "$lib/stores/settings.svelte";
    import {FunctionsStore} from "$lib/stores/functions.svelte";
    import {page} from "$app/state";

    let {data}: PageProps = $props()
    let configuration = data.configuration
    const settings = $derived(settingsStore.value)
    const listings = $derived(ListingsStore.value ?? [])
    const userListings = $derived(UserListingsStore.value)
    const fields = $derived(filteredAttributes(settings.listingFields))
    const attributes = $derived(mergedAttributes().value)
    const functions = $derived(FunctionsStore.value)
    const searchParams = $derived(ListingSearchParams)

    $effect(() => {
        if (settings && searchParams) {
            searchParams.viewAttributes = settings.listingFields
            updateListings()
        }
    })
</script>

<ListingTable {attributes} {configuration} {fields} {functions} {listings} {userListings}/>
