<script lang="ts">
    import type {Listing} from "$lib/types/Listing";
    import Table from "$lib/components/Table/Table.svelte";
    import TD from "$lib/components/Table/TD.svelte";
    import TH from "$lib/components/Table/TH.svelte";
    import type {ListingTableProps} from "$lib/components/ListingTable/ListingTable";
    import {page} from "$app/state";
    import {goto} from "$app/navigation";
    import ListingValue from "$lib/components/Value/ListingValue.svelte";
    import type {Attribute} from "$lib/types/Attribute";
    import {ListingsStore} from "$lib/stores/ListingsStore.svelte.js";
    import EditListingValue from "$lib/components/Value/EditListingValue.svelte";
    import ListingFilter from "$lib/components/ListingFilter/ListingFilter.svelte";
    import ListingDetail from "$lib/components/ListingDetail/ListingDetail.svelte";
    import {ListingDb} from "$lib/stores/ListingsDb.svelte";
    import {untrack} from "svelte";

    let {sorting, fields, attributes, configuration}: ListingTableProps = $props()

    const searchParams = $derived(ListingsStore.value.searchParams)
    let sortAscending = $derived(searchParams.sortDir === "ASC")

    let sortKey: keyof Listing = $state("")

    let p = $derived(+(page.url.searchParams.get("page") ?? 1))
    let tableData = $state<Listing[]>([])

    const partial = $derived(sorting
        .slice((p - 1) * 100, p * 100))

    let lastUpdate = $state(new Date().valueOf())

    $effect(() => {
        if (partial && lastUpdate)
            untrack(() =>
                ListingsStore.instance.fetch(partial).finally(() => {
                    Promise.all(partial
                        .map(async id => await ListingDb.get(id))
                    )
                        .then(d => {
                            tableData = d
                        })

                })
            )
    })

    const onSort = (key: string) => {
        if (searchParams.sortCol === key) {
            searchParams.sortDir = sortAscending ? "DESC" : "ASC"
        } else {
            searchParams.sortCol = key
            searchParams.sortDir = "ASC"
        }

        ListingsStore.instance.fetchSorting()
    }

    function onupdate(newValue: string, listing: Listing, attribute: Attribute) {
        const n = {
            listingId: listing.id,
            attributeId: attribute.id,
            values: [newValue]
        }

        editing = {listingId: -1, attributeId: -1}

        if (!newValue)
            ListingsStore.deleteListingValue(n).then(() => lastUpdate = new Date().valueOf())
        else if (listing[attribute.normalized]?.user != newValue)
            ListingsStore.updateListingValue(n).then(() => lastUpdate = new Date().valueOf())

    }

    function oncreate(newValue: string, listing: Listing, attribute: Attribute) {
        const n = {
            listingId: listing.id,
            attributeId: attribute.id,
            values: [newValue]
        }

        editing = {listingId: -1, attributeId: -1}
        if (newValue && newValue != listing[attribute.normalized]?.base)
            ListingsStore.createListingValue(n).then(() => lastUpdate = new Date().valueOf())

    }


    let editing = $state({listingId: -1, attributeId: -1})
    let expanded = $state<number[]>([])

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
            <button onclick={() => goto("?page=" + (p-1))} disabled={p  <= 1}>{"<"}</button>
            Page {p}
            <button onclick={() => goto("?page=" + (p+1))}
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
                    currentColumn={field.normalized}
                    label={field.label}
                    sorted={sortKey === field.normalized}
                    {sortAscending}
                    {onSort}
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
                                <EditListingValue {listing} {attribute} {oncreate} {onupdate}/>
                            {:else}
                                <ListingValue {listing} {attribute} {configuration}
                                              ondblclick={() => {editing = {listingId: listing.id, attributeId: attribute.id}}}
                                />
                            {/if}
                        {/snippet}
                    </TD>
                {/each}
            </tr>
            {#if expanded && expanded.indexOf(listing.id) > -1}
                <tr>

                    {#if listing.coordinates}
                        {@const [lat, long] = (listing.coordinates?.user ?? listing.coordinates?.base)?.toString()?.split(",") }
                        {@const addr = (listing['address']?.user ?? listing['address']?.base)?.toString() ?? "" }
                        {@const district = (listing['district']?.user ?? listing['district']?.base)?.toString() ?? "" }
                        {@const sunUrl1 = `https://voibos.rechenraum.com/voibos/voibos?Datum=06-21-13%3A00&H=10&name=sonnengang&Koordinate=${long.trim()}%2C${lat.trim()}&CRS=4326&Output=Horizont%2CLage%2CTabelle`}
                        {@const sunUrl2 = `https://voibos.rechenraum.com/voibos/voibos?Datum=06-21-13%3A00&H=10&name=sonnengang&Koordinate=${long.trim()}%2C${lat.trim()}&CRS=4326&Output=Formular%2CHorizont%2CLage%2CTabelle`}
                        {@const katasterUrl = `https://kataster.bev.gv.at/#/center/${long.trim()},${lat.trim()}/zoom/17.5/ortho/1/vermv/1`}
                        {@const laermUrl = `https://maps.laerminfo.at/#/cstrasse22_24h/bgrau/a-/q${addr}, ${district}/@${lat.trim()},${long.trim()},17z`}
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
                        <ListingDetail {listing} {attributes} {configuration} />
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
