<script lang="ts">
    import {ListingsStore} from '$lib/stores/listings.svelte'
    import type {PageProps} from "./$types";
    import ListingTable from "$lib/components/ListingTable/ListingTable.svelte";
    import {filteredAttributes} from "$lib/stores/attributes.svelte";
    import {settingsStore} from "$lib/stores/settings.svelte";


    let {data}: PageProps = $props()
    let configuration = data.configuration
    let settings = $derived(settingsStore.value)
    const listings = $derived(ListingsStore.value ?? [])
    const fields = $derived(filteredAttributes(settings.listingFields))

</script>

<ListingTable {configuration} {fields} {listings}/>
