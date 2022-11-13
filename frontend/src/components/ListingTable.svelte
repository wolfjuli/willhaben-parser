<script lang="ts">

    import {attributes} from "../stores/Attributes";
    import {onMount} from "svelte";
    import {listingOverviews} from "../stores/ListingOverviews";
    import {ListingOverview} from "../types/ListingOverview";

    let _listings: ListingOverview[] = []

    listingOverviews.subscribe(ls => {
        _listings = ls
    })

    onMount(() => {
        attributes.refresh()
        listingOverviews.refresh()
    })

    let format = Intl.NumberFormat('de-DE', {style: "currency", currency: "EUR"})

    function price(value: string | number): string {
        return format.format(value)
    }

    function score(value: string | number): number {
        return Math.round(+value * 100) / 100
    }

</script>

<h1>Listings</h1>

<div class="table-responsive">
    {#if _listings.length > 0}
        <table class="table table-striped table-sm">
            <thead>
            <tr>
                <th>ID</th>
                <th>Name</th>
                <th>User Score</th>
                <th>Calculated Score</th>
                <th>Calculated Price</th>
            </tr>
            </thead>
            <tbody>
            {#each _listings as listing}
                <tr>
                    <td>
                        <a target="_blank"
                           href="https://www.willhaben.at/iad/object?adId={listing.id}">
                            {listing.id}
                        </a>
                    </td>
                    <td>{listing.name}</td>
                    <td>{score(Object.values(listing.userScores).reduce((acc, curr) => acc + curr, 0) )}</td>
                    <td>{score(Object.values(listing.calculatedScores).reduce((acc, curr) => acc + curr, 0))}</td>
                    <td>{score(Object.values(listing.calculatedPrices).reduce((acc, curr) => acc + curr, 0))}</td>
                </tr>
            {/each}
            </tbody>
        </table>
    {:else}
        <strong> No Data found</strong>
    {/if}
</div>