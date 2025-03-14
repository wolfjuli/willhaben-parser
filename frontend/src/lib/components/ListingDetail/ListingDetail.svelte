<script lang="ts">
    import type {ListingDetailProps} from "$lib/components/ListingDetail/ListingDetail";

    let {listing, attributes, configuration}: ListingDetailProps = $props()

</script>
<dl>
    <dd>Willhaben ID</dd>
    <dt><a href={`${configuration?.listingsBaseUrl}/${listing.seoUrl}` } target="_blank">{listing.willhabenId}</a></dt>
    <dd>Points</dd>
    <dt>{listing.points}</dt>
    {#each attributes as attr}
        <dt>{attr.normalized}</dt>
        <dd>{listing[attr.normalized]}</dd>
    {/each}

</dl>
