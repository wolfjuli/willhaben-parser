<script lang="ts">
    import type {ListingDetailProps} from "$lib/components/ListingDetail/ListingDetail";
    import ListingValue from "$lib/components/Value/ListingValue.svelte";

    let {listing, attributes, configuration, horizontal = false}: ListingDetailProps = $props()
</script>
<dl>
    {#each attributes as attribute}
        <e:detail class:horizontal>
            <dt>{attribute.label}</dt>
            <dd>
                <ListingValue {listing} {attribute} {configuration} />
            </dd>
        </e:detail>
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
