<script lang="ts">
    import Modal from "$lib/components/Modal/Modal.svelte";
    import Input from "$lib/components/Input/Input.svelte";
    import ListingValue from "$lib/components/Value/ListingValue.svelte";
    import type { Listing } from "$lib/types/listing";
    import type {Attribute} from "$lib/types/Attribute";
    import {ListingsStore} from "$lib/stores/ListingsStore.svelte";
    import type {Configuration} from "$lib/types/Configuration";
    import Loading from '$images/loading.svg?component'
    import {SearchParamsStore} from "$lib/stores/SearchParamsStore.svelte";
    import {listingAttribute, setValue} from "$lib/utils/jsonpath";
    import EditListingValue from "$lib/components/Value/EditListingValue.svelte";
    import {EmptyListing} from "$lib/utils/consts";

    let {
        fields,
        open = $bindable(false),
        id = $bindable(),
        configuration
    }: {
        fields: Attribute[],
        open: boolean,
        id: number,
        configuration: Configuration
    } = $props()

    let newListing = $state<Listing>({...EmptyListing, willhabenId: id})

    let fetching = $state(false)

    function fetchWillhaben(url: string, target: HTMLInputElement) {
        target.disabled = true
        fetching = true
        ListingsStore.crawl(url).then((listing: Listing) => {
            newListing = listing
            target.value = ""
        }).finally(() => {
            target.disabled = false
            fetching = false
            if (newListing?.base?.id)
                SearchParamsStore.value.searchString = `${newListing.base.id}`
        })
    }

    let editing = $state({attributeId: -1})

    $effect(() => {
        if (!open)
            fetching = false
    })

    $effect(() => {
        id = newListing.willhabenId
    })

    async function onUpdate(newValue: string, listing: Listing, attribute: Attribute) {

        const currentAttr = listingAttribute(listing, attribute.attribute)

        if (currentAttr?.base == newValue) newValue = undefined
        if (currentAttr?.user != newValue)
            newListing = setValue(listing, `$.base.${attribute.attribute}`, newValue)

        editing = {attributeId: -1}
    }

    async function createListing() {
        ListingsStore.create(newListing).then(listing => {
            SearchParamsStore.value.searchString = `${listing.base.id}`
            newListing = {...EmptyListing, willhabenId: id}
        })
    }

</script>

<Modal title="New Entry" bind:open={open}>
    <i-new-container>
        <i-row>
            <i-title>Willhaben URL:</i-title>
            <i-input id="willhaben-url-input"><Input onsubmit={fetchWillhaben}/></i-input>
        </i-row>

        {#if fetching}
            <i-row class="loading">
                <Loading/>
            </i-row>
        {:else}
            {#each fields as field}
                <i-row>
                    <i-title>{field.label}</i-title>
                    <i-input>
                        {#if editing.attributeId === field.id }
                            <EditListingValue listing={newListing} attribute={field} onupdate={onUpdate}/>
                        {:else}
                            <ListingValue listing={newListing} attribute={field} {configuration}
                                          ondblclick={() => {editing = {attributeId: field.id}}}
                                          onclick={(newRating: number) => { if(field.dataType === "RATING") {
                                                  onUpdate(newRating, newListing, field)
                                              }} }/>
                        {/if}
                    </i-input>
                </i-row>
            {/each}
            <i-row>
                <i-title>
                    <button onclick={createListing}>Save</button>
                </i-title>
            </i-row>
        {/if}


    </i-new-container>
</Modal>

<style>
    i-new-container {
        display: flex;
        flex-direction: column;
    }

    i-row {
        display: flex;
        flex-direction: row;
        width: 100%;
        gap: 1%;
    }

    :global(i-new-container  i-row.loading svg) {
        scale: 10;
        margin: 10em auto 0;
    }

    i-title {
        width: 15%;
    }

    i-input {
        flex-grow: 1
    }
</style>