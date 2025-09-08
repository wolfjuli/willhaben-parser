<script lang="ts">
    import type {PageProps} from "./$types";
    import ListingTable from "$lib/components/ListingTable/ListingTable.svelte";
    import {filteredAttributes, mergedAttributes} from "$lib/stores/attributes.svelte";
    import {settingsStore} from "$lib/stores/settings.svelte";
    import {SearchParamsStore} from "$lib/stores/SearchParamsStore.svelte";
    import {SortingStore} from "$lib/stores/SortingStore.svelte";

    let {data}: PageProps = $props()
    let configuration = data.configuration
    const settings = $derived(settingsStore.value)
    const sorting = $derived(SortingStore.value.sorting)
    const fields = $derived(filteredAttributes(settings.listingFields))
    const attributes = $derived(mergedAttributes().value)
    const searchParams = $derived(SearchParamsStore.value)

    $effect(() => {
        if (settings && searchParams) {
            searchParams.viewAttributes = settings.listingFields
        }
    })
    $effect(() => {
        console.log("sorting triggered", sorting)
    })
</script>

<ListingTable {attributes} {sorting} {configuration} {fields}  />
