<script lang="ts">
    import type {Listing} from "$lib/types/listing";
    import Table from "$lib/components/Table/Table.svelte";
    import TD from "$lib/components/Table/TD.svelte";
    import TH from "$lib/components/Table/TH.svelte";
    import ListingValue from "$lib/components/Value/ListingValue.svelte";
    import type {Attribute} from "$lib/types/Attribute";
    import EditListingValue from "$lib/components/Value/EditListingValue.svelte";
    import ListingFilter from "$lib/components/ListingFilter/ListingFilter.svelte";
    import ListingDetail from "$lib/components/ListingDetail/ListingDetail.svelte";
    import {listingAttribute} from "$lib/utils/jsonpath";
    import {BaseAttributesStore} from "$lib/stores/Attributes.svelte";
    import {SortingStore} from "$lib/stores/SortingStore.svelte";
    import {SearchParamsStore} from "$lib/stores/SearchParamsStore.svelte";
    import {ListingsStore} from "$lib/stores/ListingsStore.svelte";
    import type {Configuration} from "$lib/types/Configuration";

    const {configuration}: {
        configuration: Configuration
    } = $props()

    const settings = $derived(SearchParamsStore.value)
    const fields = $derived(settings.viewAttributes.map(f => BaseAttributesStore.value?.find(a => f === a.attribute)).filter(Boolean)) as Attribute[]
    const attributes = $derived(BaseAttributesStore.value ?? [])

    let tableData = $state<Listing[]>()

    $effect(() => {
        const page = SearchParamsStore.value.page
        console.log("Update sorting/listing")
        SortingStore.fetch(SearchParamsStore.value)
            .then((sorting) => ListingsStore.fetch(sorting.sorting.slice((page - 1) * 100, page * 100))
                .then((listings: Listing[]) => tableData = sorting.sorting.map(s => listings.find(l => l.id === s)).filter(Boolean))
            )
    })

    let editing = $state({listingId: -1, attributeId: -1})
    let expanded = $state<number[]>([])

    const onSort = (field: Attribute) => {
        const key = field.sortingAttribute ?? field.attribute
        console.log("onSort", field, key)
        if (SearchParamsStore.value.sortCol === key) {
            SearchParamsStore.value.sortDir = (SearchParamsStore.value.sortDir === "ASC") ? "DESC" : "ASC"
        } else {
            SearchParamsStore.value.sortCol = key
            SearchParamsStore.value.sortDir = "ASC"
        }
        SearchParamsStore.set({...SearchParamsStore.value})
    }

    function onUpdate(newValue: string, listing: Listing, attribute: Attribute) {
        console.log("onUpdate", newValue)
        const currentAttr = listingAttribute(listing, attribute.attribute)
        if (currentAttr?.base == newValue) newValue = undefined
        if (currentAttr?.user != newValue)
            ListingsStore.set({
                values: newValue,
                attributeId: attribute.id,
                listingId: listing.id
            })
        editing ={ attributeId: -1, listingId: -1}
    }

    function toggleExpand(id: number) {
        const idx = expanded.indexOf(id)
        if (idx > -1)
            expanded = [...expanded.slice(0, idx), ...expanded.slice(idx + 1)]
        else
            expanded = [...expanded, id]
    }
</script>

{#if expanded && fields}
    <div class=container-fluid>
        <div class=col>
            <button onclick={() => SearchParamsStore.value.page--}
                    disabled={SearchParamsStore.value.page  <= 1}>{"<"}</button>
            Page {SearchParamsStore.value.page}
            <button onclick={() => SearchParamsStore.value.page++}
                    disabled={(tableData ?? []).length < 100}>{">"}</button>
        </div>
        <div class=col>
            <ListingFilter {attributes}></ListingFilter>
        </div>
    </div>

    <Table {tableData}>
        {#snippet thead()}
            <TH></TH>
            {#each fields as field}
                <TH
                    currentColumn={field.attribute}
                    label={field.label}
                    sorted={SearchParamsStore.value.sortCol === field.attribute}
                    sortAscending={SearchParamsStore.value.sortDir === "ASC"}
                    onSort={() => onSort(field)}
                ></TH>
            {/each}
        {/snippet}

        {#snippet row(listing: Listing, idx)}
            <tr class:even={idx % 2}>
                <TD>
                    {#snippet render()}
                        <button onclick={() => toggleExpand(listing.id)}>V</button>
                    {/snippet}
                </TD>
                {#each fields as attribute}
                    <TD>
                        {#snippet render()}
                            {#if editing.attributeId === attribute.id && editing.listingId === listing.id}
                                <EditListingValue {listing} {attribute} onupdate={onUpdate}/>
                            {:else}
                                <ListingValue {listing} {attribute} {configuration}
                                              ondblclick={() => {editing = {listingId: listing.id, attributeId: attribute.id}}}
                                />
                            {/if}
                        {/snippet}
                    </TD>
                {/each}
            </tr>
            {#if expanded && expanded.indexOf(listing?.id) > -1}
                {@const coords = listingAttribute(listing, 'attributeMap.coordinates')}
                {@const address = listingAttribute(listing, 'attributeMap.address')}
                {@const district = listingAttribute(listing, 'attributeMap.district')}
                <tr>
                    {#if coords.user || coords.base}
                        {@const [lat, long] = (coords.user ?? coords.base)?.toString()?.split(",") ?? []}
                        {@const addr = (address.user ?? address.base)?.toString() ?? "" }
                        {@const distr = (district?.user ?? district?.base)?.toString() ?? "" }
                        {@const
                            sunUrl1 = `https://voibos.rechenraum.com/voibos/voibos?Datum=06-21-13%3A00&H=10&name=sonnengang&Koordinate=${long.trim()}%2C${lat.trim()}&CRS=4326&Output=Horizont%2CLage%2CTabelle`}
                        {@const
                            sunUrl2 = `https://voibos.rechenraum.com/voibos/voibos?Datum=06-21-13%3A00&H=10&name=sonnengang&Koordinate=${long.trim()}%2C${lat.trim()}&CRS=4326&Output=Formular%2CHorizont%2CLage%2CTabelle`}
                        {@const
                            katasterUrl = `https://kataster.bev.gv.at/#/center/${long.trim()},${lat.trim()}/zoom/17.5/ortho/1/vermv/1`}
                        {@const
                            laermUrl = `https://maps.laerminfo.at/#/cstrasse22_24h/bgrau/a-/q${addr}, ${distr}/@${lat.trim()},${long.trim()},17z`}
                        <td colspan="2"><a target="_blank" href={katasterUrl}>Kataster</a>
                            <iframe src={katasterUrl}></iframe>
                        </td>
                        <td colspan="2">
                            <a target="_blank" href={sunUrl2}>Sonnestand</a>
                            <iframe src={sunUrl1} class="suncalc"></iframe>
                        </td>
                        <td colspan="2">
                            <a target="_blank" href={laermUrl}>Lärm</a>
                            <iframe src={laermUrl}></iframe>
                        </td>
                    {:else}
                        <td>Kataster</td>
                        <td>Sonnestand</td>
                        <td>Lärm</td>
                    {/if}
                    <td colspan={fields.length - 6 } class="details">
                        <ListingDetail {listing} {attributes} {configuration}/>
                    </td>
                </tr>
            {/if}
        {/snippet}

    </Table>
{:else }
    No data available
{/if}


<style>
    iframe {
        width: 30vw;
        height: 50vh;
    }

    .details {
        display: block;
        overflow: scroll;
        height: 60vh;
        width: 30vw;
    }

    .suncalc {
        width: 60vw;
        height: 100vh;
        transform: scale(0.5);
        margin: -24vh -15vw -25vh -15vw;
    }
</style>
