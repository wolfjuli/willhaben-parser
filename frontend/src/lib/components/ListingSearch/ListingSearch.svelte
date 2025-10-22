<script lang="ts">
    import type {ListingSearchProps} from "$lib/components/ListingSearch/ListingSearch";
    import {ListingsStore} from "$lib/stores/ListingsStore.svelte";
    import {SearchParamsStore} from "$lib/stores/SearchParamsStore.svelte";

    let {sorted, onselect}: ListingSearchProps = $props()

    function onselected(ev) {
        const id = +(ev?.explicitOriginalTarget?.value ?? ev?.target?.value ?? 0)
        id ? onselect(ListingsStore.get(id)) : onselect(undefined)
    }
</script>


<div role="group">
    <input bind:value={SearchParamsStore.value.searchString} onkeyup={() => {if(SearchParamsStore.value.searchString === "") onselected(undefined)}}
           type="search"/>
    <button onclick={() => {SearchParamsStore.value.searchString = ""; onselected(undefined)}}>X</button>
</div>

<select size="5" onclick={onselected}>
    {#each sorted as id }
        {@const listing = ListingsStore.get(id)}
        {#if listing}
            <option
                value={id}>{`${listing.willhabenId} - ${`${listing.base.heading}`.slice(0, 30)} - ${listing.points} - ${listing.base.priceForDisplay}`}</option>
        {/if}
    {/each}
</select>
