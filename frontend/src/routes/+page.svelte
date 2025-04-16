<script lang="ts">
    import { ListingsStore} from '$lib/stores/ListingsStore.svelte.js'
    import type {PageProps} from "./$types";
    import ListingTable from "$lib/components/ListingTable/ListingTable.svelte";
    import {filteredAttributes, mergedAttributes} from "$lib/stores/attributes.svelte";
    import {settingsStore} from "$lib/stores/settings.svelte";

    let {data}: PageProps = $props()
    let configuration = data.configuration
    const settings = $derived(settingsStore.value)
    const sorting = $derived(ListingsStore.value.sorting)
    const fields = $derived(filteredAttributes(settings.listingFields))
    const attributes = $derived(mergedAttributes().value)
    const searchParams = $derived(ListingsStore.value.searchParams)

    $effect(() => {
        if (settings && searchParams) {
            searchParams.viewAttributes = settings.listingFields
        }
    })
</script>

<ListingTable {attributes} {sorting} {configuration} {fields}  />
