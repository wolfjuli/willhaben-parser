<script lang="ts">
    import type {PageProps} from "./$types";
    import ListingTable from "$lib/components/ListingTable/ListingTable.svelte";
    import {settingsStore} from "$lib/stores/settings.svelte";
    import {SearchParamsStore} from "$lib/stores/SearchParamsStore.svelte";
    import {SortingStore} from "$lib/stores/SortingStore.svelte";
    import {BaseAttributesStore} from "$lib/stores/Attributes.svelte";

    let {data}: PageProps = $props()
    const settings = $derived(settingsStore.value)
    const sorting = $derived(SortingStore.value.sorting)
    const searchParams = $derived(SearchParamsStore.value)
    const fields = $derived(settings.listingFields.map(f => BaseAttributesStore.value?.find(a => f === a.attribute)).filter(Boolean))
    const attributes = $derived(BaseAttributesStore.value?.filter(a => !fields.find(f => f!!.attribute === a.attribute)) ?? [])

    $effect(() => {
        if (settings && searchParams) {
            searchParams.viewAttributes = settings.listingFields
        }
    })
    $effect(() => {
        console.log("sorting triggered", sorting)
    })
</script>

<ListingTable {attributes} configuration={data.configuration} {fields} {sorting}/>
