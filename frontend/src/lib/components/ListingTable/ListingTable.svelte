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
    import {ListingSearchParams, ListingsStore} from "$lib/stores/ListingsStore.svelte.js";
    import EditListingValue from "$lib/components/Value/EditListingValue.svelte";
    import ListingFilter from "$lib/components/ListingFilter/ListingFilter.svelte";
    import ListingDetail from "$lib/components/ListingDetail/ListingDetail.svelte";

    let {listings, sorting,  fields, attributes, configuration }: ListingTableProps = $props()

    const listingsStore = $derived(ListingsStore.value)

    let sortKey: keyof Listing = $state("")

    let sortAscending = $derived(ListingSearchParams.sortDir === "ASC")
    let p = $derived(+(page.url.searchParams.get("page") ?? 1))
    let tableData = $derived(sorting
        .slice((p - 1) * 100, p  * 100)
        .map (id => listings[id]))

    const onSort = (key: string) => {
        if(ListingSearchParams.sortCol === key)
                ListingSearchParams.sortDir = sortAscending ? "DESC" : "ASC"
        else
        {
            ListingSearchParams.sortCol = key
            ListingSearchParams.sortDir = "ASC"
        }

    }

    function onupdate(newValue: string, listing: Listing, attribute: Attribute) {
        const n = {
            listingId: listing.id,
            attributeId: attribute.id,
            values: [newValue]
        }
        if (!newValue)
            listingsStore.deleteListingValue(n)
        else if (listing[attribute.normalized]?.user != newValue)
            listingsStore.updateListingValue(n)

        editing= {listingId: -1, attributeId: -1}
    }

    function oncreate(newValue: string, listing: Listing, attribute: Attribute) {
        const n = {
            listingId: listing.id,
            attributeId: attribute.id,
            values: [newValue]
        }
        if (newValue && newValue != listing[attribute.normalized]?.base)
            listingsStore.createListingValue(n)

        editing = {listingId: -1, attributeId: -1}
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
            <ListingFilter {attributes} ></ListingFilter>
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
                            {#if editing.attributeId === attribute.id && editing.listingId === listing.willhabenId}
                                <EditListingValue {listing} {attribute} {oncreate} {onupdate}/>
                            {:else}
                                <ListingValue {listing} {attribute} {configuration} ondblclick={() => {editing = {listingId: listing.id, attributeId: attribute.id}}}
                                />
                            {/if}
                        {/snippet}
                    </TD>
                {/each}
            </tr>
            {#if expanded.indexOf(listing.willhabenId) > -1}
                <tr>
                    <td colspan={fields.length}>
                        <ListingDetail {listing} {attributes} {configuration} />
                    </td>
                </tr>
            {/if}
        {/snippet}

    </Table>
{:else }
    No data available
{/if}
