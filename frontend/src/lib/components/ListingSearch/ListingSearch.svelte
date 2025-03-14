<script lang="ts">
    import type {ListingSearchProps} from "$lib/components/ListingSearch/ListingSearch";
    import type {Listing} from "$lib/types/Listing";
    import {listingFilter} from "$lib/utils/listingFilter";

    let {listings, onselect}: ListingSearchProps = $props()

    let searchTerm = $state("")

    let listingsMapping: {[key: number]: Listing} = $derived(listings
        .filter(listingFilter(searchTerm))
        .reduce((acc, l) => {
            acc[l.willhabenId] = l
            return acc
        }, {})
    )

    function onselected(ev) {
        const willHabenId = ev?.explicitOriginalTarget?.value
        searchTerm = willHabenId ?? ""
        onselect(willHabenId ? listingsMapping[willHabenId] : undefined)
    }
</script>


<div role="group">
<input type="search" bind:value={searchTerm} onkeyup={() => {if(searchTerm === "") onselected(undefined)}}/>
    <button onclick={() => {searchTerm = ""; onselected(undefined)}}>X</button>
</div>

<select size="5" onclick={onselected}>
    {#each Object.keys(listingsMapping).toSorted() as willHabenId }
        <option
            value={willHabenId}>{`${willHabenId} - ${listingsMapping[willHabenId].heading.slice(0, 30)} - ${listingsMapping[willHabenId].points} - ${listingsMapping[willHabenId].priceForDisplay}`}</option>
    {/each}
</select>
