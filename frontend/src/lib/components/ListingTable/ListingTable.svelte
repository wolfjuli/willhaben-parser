<script lang="ts">
    import type {Listing} from "$lib/types/Listing";
    import Table from "$lib/components/Table/Table.svelte";
    import TD from "$lib/components/Table/TD.svelte";
    import TH from "$lib/components/Table/TH.svelte";
    import type {ListingTableProps} from "$lib/components/ListingTable/ListingTable";
    import {page} from "$app/state";
    import {goto} from "$app/navigation";
    import {transformListing} from "$lib/utils/transformListing.js";
    import ListingValue from "$lib/components/Value/ListingValue.svelte";
    import type {Attribute} from "$lib/types/Attribute";
    import {createListingValue, ListingSearchParams, updateListingValue} from "$lib/stores/listings.svelte";
    import {deleteListingValue} from "$lib/stores/listings.svelte.js";
    import EditListingValue from "$lib/components/Value/EditListingValue.svelte";
    import ListingFilter from "$lib/components/ListingFilter/ListingFilter.svelte";
    import ListingDetail from "$lib/components/ListingDetail/ListingDetail.svelte";

    let {listings, userListings, fields, attributes, configuration, functions}: ListingTableProps = $props()

    let sortKey: keyof Listing = $state("")

    let sortAscending = $derived(ListingSearchParams.sortDirection === "ASC")
    let tableData = $derived(listings)

    const onSort = (key: string) => {
        if(ListingSearchParams.sortCol === key)
                ListingSearchParams.sortDirection = sortAscending ? "DESC" : "ASC"
        else
        {
            ListingSearchParams.sortCol = key
            ListingSearchParams.sortDirection = "ASC"
        }

    }

    function onupdate(newValue: string, listing: Listing, attribute: Attribute) {
        const n = {
            listingId: listing.id,
            attributeId: attribute.id,
            values: [newValue]
        }
        if (!newValue)
            deleteListingValue(n)
        else if (listing[attribute.normalized] != newValue)
            updateListingValue(n)

        editing = -1
    }

    function oncreate(newValue: string, listing: Listing, attribute: Attribute) {
        const n = {
            listingId: listing.id,
            attributeId: attribute.id,
            values: [newValue]
        }
        if (newValue && newValue != listing[attribute.normalized])
            createListingValue(n)

        editing = {willhabenId: -1, attributeId: -1}
    }


    let editing = $state({willhabenId: -1, attributeId: -1})

    let expanded = $state<number[]>([])

</script>

{#if fields}
    <div class=container-fluid>
        <div class=col>
            <button onclick={() => ListingSearchParams.page -= 1} disabled={ListingSearchParams.page  <= 1}>{"<"}</button>
            Page {ListingSearchParams.page}
            <button onclick={() => ListingSearchParams.page += 1}
                    disabled={(listings ?? []).length === 0}>{">"}</button>
        </div>
        <div class=col>
            <ListingFilter {attributes} onchange={fn => filterFn = fn}></ListingFilter>
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
                            {#if editing.attributeId === attribute.id && editing.willhabenId === listing.willhabenId}
                                <EditListingValue {listing} userListing={userListings[listing.willhabenId]} {attribute}
                                                  {oncreate} {onupdate}/>
                            {:else}
                                <ListingValue {listing} {attribute} {configuration}
                                              userListing={userListings?.[listing.willhabenId]}
                                              ondblclick={() => {editing = {willhabenId: listing.willhabenId, attributeId: attribute.id}; console.log(editing)}}
                                />
                            {/if}
                        {/snippet}
                    </TD>
                {/each}
            </tr>
            {#if expanded.indexOf(listing.willhabenId) > -1}
                <tr>
                    <td colspan={fields.length}>
                        <ListingDetail {listing} {attributes} {configuration}
                                       userListing={userListings?.[listing.willhabenId]} horizontal={true}/>
                    </td>
                </tr>
            {/if}
        {/snippet}

    </Table>
{:else }
    No data available
{/if}
