<script lang="ts">
    import type {ListingDetailProps} from "$lib/components/ListingDetail/ListingDetail";
    import ListingValue from "$lib/components/Value/ListingValue.svelte";
    import type {Listing} from "$lib/types/Listing";
    import type {Attribute} from "$lib/types/Attribute";
    import {createListingValue, deleteListingValue, updateListingValue} from "$lib/stores/listings.svelte";
    import EditListingValue from "$lib/components/Value/EditListingValue.svelte";

    let {listing, userListing, attributes, configuration, horizontal = false}: ListingDetailProps = $props()


    function onupdate(newValue: string, listing: Listing, attribute: Attribute) {
        const n = {
            listingId: listing.id,
            attributeId: attribute.id,
            values: [newValue]
        }
        if (!newValue)
            deleteListingValue(n)
        else if (listing[attribute.normalized] != newValue)
            updateListingValue(n)

        editing = -1
    }

    function oncreate(newValue: string, listing: Listing, attribute: Attribute) {
        const n = {
            listingId: listing.id,
            attributeId: attribute.id,
            values: [newValue]
        }
        if (newValue && newValue != listing[attribute.normalized])
            createListingValue(n)

        editing = -1
    }

    let editing = $state(-1)
</script>
<dl>
    {#each attributes as attribute}
        <e:detail class:horizontal>
            <dt>{attribute.label}</dt>
            <dd>
                {#if editing === attribute.id}
                    <EditListingValue {listing} {userListing} {attribute} {oncreate} {onupdate}/>
                {:else}
                    <ListingValue {listing} {userListing} {attribute} {configuration}
                                  ondblclick={() => editing = attribute.id}/>
                {/if}
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
