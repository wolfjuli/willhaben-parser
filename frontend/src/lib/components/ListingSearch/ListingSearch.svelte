<script lang="ts">
    import type {ListingSearchProps} from "$lib/components/ListingSearch/ListingSearch";
    import type {Listing} from "$lib/types/Listing";
    import {listingFilter} from "$lib/utils/listingFilter";

    let {listings, sorted, onselect}: ListingSearchProps = $props()

    let searchTerm = $state("")

    function onselected(ev) {
        const willhabenId = ev?.explicitOriginalTarget?.value ?? ev.target.value
        searchTerm = willhabenId ?? ""
        onselect(willhabenId ? listingsMapping[willhabenId] : undefined)
    }
</script>


<div role="group">
<input type="search" bind:value={searchTerm} onkeyup={() => {if(searchTerm === "") onselected(undefined)}}/>
    <button onclick={() => {searchTerm = ""; onselected(undefined)}}>X</button>
</div>

<select size="5" onclick={onselected}>
    {#each sorted as id }
        <option
            value={id}>{`${id} - ${listings[id].heading.base.slice(0, 30)} - ${listings[id].points.base} - ${listings[id].priceForDisplay.base}`}</option>
    {/each}
</select>
