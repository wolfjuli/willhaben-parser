<script lang="ts">
    import type {ListingSearchProps} from "$lib/components/ListingSearch/ListingSearch";
    import type {Listing} from "$lib/types/Listing";

    let {listings, onselect}: ListingSearchProps = $props()

    let searchTerm = $state("")

    let listingsMapping: {[key: number]: Listing} = $derived(listings
        .filter(l => l.willhabenId.toString().includes(searchTerm) ||
            (l.price?.toString()?.includes(searchTerm) ?? false) ||
            (l.heading?.toString()?.includes(searchTerm) ?? false) ||
            (l.bodyDyn?.toString()?.includes(searchTerm) ?? false)
        )
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
        <option value={willHabenId} >{`${willHabenId} - ${listingsMapping[willHabenId].heading} - ${listingsMapping[willHabenId].priceForDisplay}`}</option>
    {/each}
</select>
