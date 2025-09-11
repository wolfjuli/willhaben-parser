<script lang="ts">
    import type {ListingDetailProps} from "$lib/components/ListingDetail/ListingDetail";
    import ListingValue from "$lib/components/Value/ListingValue.svelte";

    let {listing, attributes, configuration}: ListingDetailProps = $props()
</script>
<dl>
    {#each attributes as attribute}
        <dt>{attribute.label ?? attribute.attribute}</dt>
        <dd>
            <ListingValue {listing} {attribute} {configuration}/>
        </dd>
    {/each}
</dl>

<style>
    .horizontal {
        display: block;
        float: left;
    }

    dl {
        clear: both
    }
</style>
