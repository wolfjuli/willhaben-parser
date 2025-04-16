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
    import {FetchingStore} from "$lib/stores/FetchingStore.svelte";

    let {sorting, fields, attributes, configuration}: ListingTableProps = $props()

    const listingsStore = $derived(ListingsStore.value)
    const searchParams = $derived(listingsStore.searchParams)
    let sortAscending = $derived(searchParams.sortDir === "ASC")

    let sortKey: keyof Listing = $state("")

    let p = $derived(+(page.url.searchParams.get("page") ?? 1))
    let tableData = $state<Listing[]>([])

    $effect(() => {
        if (listingsStore.knownMd5)
            Promise.all(sorting
                .slice((p - 1) * 100, p * 100)
                .map(async id => await ListingDb.get(id)))
                .then(d => tableData = d)
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
        FetchingStore.whileFetching("editValue", () => {
            const n = {
                listingId: listing.id,
                attributeId: attribute.id,
                values: [newValue]
            }

            editing = {listingId: -1, attributeId: -1}
            if (!newValue)
                return ListingsStore.deleteListingValue(n)
            else if (listing[attribute.normalized]?.user != newValue)
                return ListingsStore.updateListingValue(n)
        })
    }

    function oncreate(newValue: string, listing: Listing, attribute: Attribute) {
        FetchingStore.whileFetching("editValue", () => {
            const n = {
                listingId: listing.id,
                attributeId: attribute.id,
                values: [newValue]
            }

            editing = {listingId: -1, attributeId: -1}

            if (newValue && newValue != listing[attribute.normalized]?.base)
                return ListingsStore.createListingValue(n)
        })
    }


    let editing = $state({listingId: -1, attributeId: -1})

    let expanded = $state<number[]>([])

</script>

{#if fields}
    <div class=container-fluid>
        <div class=col>
            <button onclick={() => goto("?page=" + (p-1))} disabled={p  <= 1}>{"<"}</button>
            Page {p}
            <button onclick={() => goto("?page=" + (p+1))}
                    disabled={(tableData ?? []).length === 0}>{">"}</button>
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
                        <button>V</button>
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
            {#if expanded.indexOf(listing.id) > -1}
                <tr>
                    <td colspan={fields.length}>
                        <ListingDetail {listing} {attributes} {configuration}/>
                    </td>
                </tr>
            {/if}
        {/snippet}

    </Table>
{:else }
    No data available
{/if}
