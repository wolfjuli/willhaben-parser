<script lang="ts">
    import type {ListingSearchProps} from "$lib/components/ListingSearch/ListingSearch";
    import {ListingsStore} from "$lib/stores/ListingsStore.svelte";

    let {listings, sorted, onselect}: ListingSearchProps = $props()

    function onselected(ev) {
        const id = ev?.explicitOriginalTarget?.value ?? ev?.target?.value
        onselect(id ? listings[id] : undefined)
    }
</script>


<div role="group">
<input type="search" bind:value={ListingsStore.value.searchParams.searchString} onkeyup={() => {if(ListingsStore.value.searchParams.searchString === "") onselected(undefined)}}/>
    <button onclick={() => {ListingsStore.value.searchParams.searchString = ""; onselected(undefined)}}>X</button>
</div>

<select size="5" onclick={onselected}>
    {#each sorted as id }
        {@const listing = listings[id]}
        <option
            value={id}>{`${listing.willhabenId} - ${listing.heading.base.slice(0, 30)} - ${listing.points} - ${listing.priceForDisplay.base}`}</option>
    {/each}
</select>
