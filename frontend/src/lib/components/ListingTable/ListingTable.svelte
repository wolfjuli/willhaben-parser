<script lang="ts">
    import type {Listing} from "$lib/types/Listing";
    import Table from "$lib/components/Table/Table.svelte";
    import TD from "$lib/components/Table/TD.svelte";
    import TH from "$lib/components/Table/TH.svelte";
    import type {ListingTableProps} from "$lib/components/ListingTable/ListingTable";
    import {page} from "$app/state";
    import {goto} from "$app/navigation";
    import {listingFilter} from "$lib/utils/listingFilter";

    let {listings, fields, configuration}: ListingTableProps = $props()

    let sortKey: keyof Listing = $state("")
    let searchTerm = $state("")
    let currentPage = $derived(+(page.url.searchParams.get('page') ?? 1));
    let sortAscending = $state(false)
    let sortFn: (a: Listing, b: Listing) => number = $state(() => 0)
    let tableData = $derived(
        ((configuration ? listings : []) ?? [])
            .filter(listingFilter(searchTerm))
            .toSorted(sortFn)
            .slice(100 * (currentPage - 1), 100 * currentPage)
            .map(l => {
                    return ({
                        ...l,
                        seoUrl: configuration.listingsBaseUrl + "/" + l.seoUrl,
                        mmo: configuration.imageBaseUrl + "/" + l.mmo
                    })
                }
            )
    )


    const sorting = (key: keyof Listing) => {
        sortAscending = key === sortKey ? !sortAscending : true
        sortKey = key

        return (
            left: Listing,
            right: Listing,
        ): number => {
            let sk = sortKey === "priceForDisplay" ? "price" : sortKey

            let [a, b] = [left[sk], right[sk]]
            if (!sortAscending) [b, a] = [a, b]
            if (typeof a === 'number' || typeof b === 'number') return a - b
            else return JSON.stringify(a ?? "").localeCompare(JSON.stringify(b ?? ""))
        }
    }

    const onSort = (key: string) => {
        sortFn = sorting(key as keyof Listing)
    }

</script>

{#if fields}
    <div class=container-fluid>
        <div class=col>
            <button onclick={() => goto("?page=" + (currentPage -1))} disabled={currentPage <= 1}>{"<"}</button>
            Page {currentPage}
            <button onclick={() => goto("?page=" + (currentPage +1))}
                    disabled={currentPage >= (listings ?? []).length / 100}>{">"}</button>
        </div>
        <div class=col>

            <div role="group">
                <input type="search" bind:value={searchTerm}/>
                <button onclick={() => {searchTerm = "" }}>X</button>
            </div>

        </div>
    </div>

    <Table {tableData}>
        {#snippet thead()}
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

        {#snippet row(obj: Listing, idx)}
            <tr class:even={idx % 2}>
                {#each fields as field}
                    <TD>
                        {#snippet render()}
                            {#if field.dataType === "LINK" }
                                <a href={obj[field.normalized].toString()} target="_blank">
                                    {obj[field.normalized]}
                                </a>
                            {:else if field.dataType === "IMAGE"}
                                <img src={obj[field.normalized].toString()} alt={obj[field.normalized].toString()}/>
                            {:else}
                                {obj[field.normalized]}
                            {/if}
                        {/snippet}
                    </TD>
                {/each}
            </tr>
        {/snippet}

    </Table>
{:else }
    No data available
{/if}
