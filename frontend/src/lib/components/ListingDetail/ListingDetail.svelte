<script lang="ts">
    import type {ListingDetailProps} from "$lib/components/ListingDetail/ListingDetail";
    import {transformListing} from "$lib/utils/transformListing";

    let {listing, attributes, configuration, functions}: ListingDetailProps = $props()

    let transformed = $derived(transformListing(listing, attributes, functions))
</script>
<dl>
    {#each attributes as attr}
        <dt>{attr.label}</dt>
        <dd>{transformed[attr.normalized]}</dd>
    {/each}

</dl>
