<script lang="ts">
    import Table from '$lib/components/Table/Table.svelte'
    import TH from '$lib/components/Table/TH.svelte'
    import TD from '$lib/components/Table/TD.svelte'
    import {ConfigurationStore} from '$lib/stores/configuration.svelte'
    import {ListingsStore} from '$lib/stores/listings.svelte'
    import {page} from '$app/state'
    import {goto} from '$app/navigation'
    import type {Listing} from "$lib/types/Listing";

    let currentPage = $derived(+(page.url.searchParams.get('page') ?? 1));
    let config = ConfigurationStore
    let listings = ListingsStore

    let sortFn: (a: Listing, b: Listing) => number = $state(() => 0)
    let tableData = $derived(
        (listings?.value ?? [])
            .toSorted(sortFn)
            .slice(100 * (currentPage - 1), 100 * currentPage)
            .map(l =>
                ({
                    ...l,
                    SEO_URL: config.value?.listingsBaseUrl + "/" + l.SEO_URL,
                    MMO: config.value?.imageBaseUrl + "/" + l.MMO,
                    ESTATE_SIZE_LIVING_AREA: l["ESTATE_SIZE/LIVING_AREA"]
                })
            )
    )

    const fields = [
        {name: "MMO", label: "", type: "text"},
        {name: "HEADING", label: "Titel", type: "text"},
        {name: "PROPERTY_TYPE", label: "Typ", type: "text"},
        {name: "DISTRICT", label: "Bezirk", type: "text"},
        {name: "ADDRESS", label: "Addresse", type: "text"},
        {
            name: "PRICE_FOR_DISPLAY",
            label: "Preis",
            type: "text"
        },
        {name: "ISPRIVATE", label: "Makler", type: "text"},
        {name: "ESTATE_SIZE", label: "Fläche", type: "text"},
        {name: "ESTATE_SIZE_LIVING_AREA", label: "Wohnfläche", type: "text"}
    ]

    let sortKey: keyof Listing = $state("")
    let sortAscending = $state(false)


    const sorting = (key: keyof Listing) => {
        sortAscending = key === sortKey ? !sortAscending : true
        sortKey = key

        return (
            left: Listing,
            right: Listing,
        ): number => {
            let sk = sortKey === "PRICE_FOR_DISPLAY" ? "PRICE" : sortKey

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

<button onclick={() => goto("?page=" + (currentPage -1))} disabled={currentPage <= 1}>{"<"}</button>
Page {currentPage}
<button onclick={() => goto("?page=" + (currentPage +1))}
        disabled={currentPage >= (listings ?? []).length / 100}>{">"}</button>

<Table {tableData}>
    {#snippet thead()}
        {#each fields as field}
            <TH
                currentColumn={field.name}
                label={field.label}
                sorted={sortKey === field.name}
                {sortAscending}
                {onSort}
            ></TH>
        {/each}
    {/snippet}

    {#snippet row(obj, idx)}
        <tr class:even={idx % 2}>
            {#each fields as field}
                <TD>
                    {#snippet render()}
                        {#if field.name === "MMO" }
                            <a href={obj["SEO_URL"]} target="_blank">
                                <img src={obj[field.name]} alt="Logo"/>
                            </a>
                        {:else if field.name === "HEADING"}
                            <a href={obj["SEO_URL"]} target="_blank">{obj[field.name]}</a>
                        {:else}
                            {obj[field.name]}
                        {/if}
                    {/snippet}
                </TD>
            {/each}
        </tr>
    {/snippet}

</Table>
