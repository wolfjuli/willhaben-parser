<script lang="ts">
    import type {ListingSearchProps} from "$lib/components/ListingSearch/ListingSearch";
    import {ListingsStore} from "$lib/stores/ListingsStore.svelte";
    import {ListingDb} from "$lib/stores/ListingsDb.svelte";

    let {sorted, onselect}: ListingSearchProps = $props()

    function onselected(ev) {
        const id = +(ev?.explicitOriginalTarget?.value ?? ev?.target?.value ?? 0)
        id ? ListingDb.get(id).then(l => onselect(l)) : onselect(undefined)
    }
</script>


<div role="group">
    <input type="search" bind:value={ListingsStore.value.searchParams.searchString}
           onkeyup={() => {if(ListingsStore.value.searchParams.searchString === "") onselected(undefined)}}/>
    <button onclick={() => {ListingsStore.value.searchParams.searchString = ""; onselected(undefined)}}>X</button>
</div>

<select size="5" onclick={onselected}>
    {#each sorted as id }
        {#await ListingDb.get(id) then listing}
            <option
                value={id}>{`${listing.willhabenId} - ${listing.heading.base.slice(0, 30)} - ${listing.points} - ${listing.priceForDisplay.base}`}</option>
        {/await}
    {/each}
</select>
