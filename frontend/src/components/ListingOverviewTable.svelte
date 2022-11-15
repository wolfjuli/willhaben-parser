<script lang="ts">

    import {attributes} from "../stores/Attributes";
    import {onMount} from "svelte";
    import {listingOverviews} from "../stores/ListingOverviews";
    import {ListingOverview} from "../types/ListingOverview";
    import TD from "./table/TD.svelte";
    import {price, score} from "../modules/Extensions";
    import ListingTable from "./ListingTable.svelte";
    import PropertyTable from "./PropertyTable.svelte";

    let _listings: ListingOverview[] = []

    listingOverviews.subscribe(ls => {
        _listings = ls
    })

    onMount(() => {
        attributes.refresh()
        listingOverviews.refresh()
    })



</script>

<h1>Listings</h1>

<div class="table-responsive">
    {#if _listings.length > 0}
        <table class="table table-striped table-sm table-hover">
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
                    <td id={listing.id}>
                        <a target="_blank"
                           href="https://www.willhaben.at/iad/object?adId={listing.id}">
                            {listing.id}
                        </a>
                    </td>
                    <TD title="{listing.name}">
                        <ListingTable id={listing.id}></ListingTable>
                    </TD>
                    <TD title={score(Object.values(listing.userScores).reduce((acc, curr) => acc + curr, 0) )}></TD>
                    <TD title={score(Object.values(listing.calculatedScores).reduce((acc, curr) => acc + curr, 0))}>
                        <PropertyTable object={listing.calculatedScores}></PropertyTable>
                    </TD>
                    <TD title={price(Object.values(listing.calculatedPrices).reduce((acc, curr) => acc + curr, 0))}>
                        <PropertyTable object={listing.calculatedPrices}></PropertyTable>
                    </TD>
                </tr>
            {/each}
            </tbody>
        </table>
    {:else}
        <strong>Loading...</strong>
    {/if}
</div>