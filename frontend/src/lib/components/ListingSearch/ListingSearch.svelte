<script lang="ts">
    import type {ListingSearchProps} from "$lib/components/ListingSearch/ListingSearch";
    import type {Listing} from "$lib/types/Listing";
    import {listingFilter} from "$lib/utils/listingFilter";
    import {ListingsStore} from "$lib/stores/ListingsStore.svelte";

    let {listings, sorted, onselect}: ListingSearchProps = $props()

    let searchTerm = $state("")
    let filterAttributes = ["address", "heading", "description", "willhabenId"]
    let filterFn = $derived<(l: Listing) => boolean>(l => searchTerm === "" || l.willhabenId.toString().indexOf(searchTerm) > -1 ||  )
    let filtered = $derived(sorted.filter(s => filterFn(listings[s])))


    function onselected(ev) {
        const id = ev?.explicitOriginalTarget?.value ?? ev.target.value
        searchTerm = id ?? ""
        onselect(id ? listings[id] : undefined)
    }
</script>


<div role="group">
<input type="search" bind:value={searchTerm} onkeyup={() => {if(searchTerm === "") onselected(undefined)}}/>
    <button onclick={() => {searchTerm = ""; onselected(undefined)}}>X</button>
</div>

<select size="5" onclick={onselected}>
    {#each filtered as id }
        {@const listing = listings[id]}
        <option
            value={id}>{`${listing.willhabenId} - ${listing.heading.base.slice(0, 30)} - ${listing.points} - ${listing.priceForDisplay.base}`}</option>
    {/each}
</select>
