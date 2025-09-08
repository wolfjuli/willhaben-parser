<script lang="ts">
    import type {ListingValueProps} from "$lib/components/Value/ListingValue";
    import {listingAttribute} from "$lib/utils/jsonpath.js";

    let {
        listing, attribute, configuration,
        onclick = () => {
        },
        ondblclick = () => {
        }
    }: ListingValueProps = $props()

    let attr = $derived(listing && attribute ? listingAttribute(listing, attribute.attribute) : undefined)
    let val = $derived(attr?.custom?.toString() ?? attr?.base?.toString() ?? "[empty]")
    let userVal = $derived(attr?.user?.toString())
    let obj = $derived(attr?.custom as unknown as { href: string, value: string })

    $effect(() => {
        if (listing.id === 3826) console.log(listing, attribute.attribute, attr, val, userVal, obj)
    })
</script>
<span onclick={() => onclick(listingAttribute(listing, attribute.attribute), listing)}
      ondblclick={() => ondblclick(listingAttribute(listing, attribute.attribute), listing)}>
{#if attribute.dataType === "LINK" }
    {#if obj && obj.href}
        <a href={configuration.listingsBaseUrl + '/' + obj.href} target="_blank">{obj.value}</a>
    {:else}
        <a href={configuration.listingsBaseUrl + `/${val}`} target="_blank">{val}</a>
    {/if}
{:else if attribute.dataType === "IMAGE"}
    <img src={configuration.imageBaseUrl + `/${val}`} alt={val} />
{:else}
    <div>
    <span class:strike={userVal}>
        {val}
    </span>
        {#if userVal}{userVal}{/if}
    </div>
{/if}
</span>

<style>
    .strike {
        text-decoration: line-through;
        display: block;
    }

    img {
        max-height: 20vh;
    }
</style>
