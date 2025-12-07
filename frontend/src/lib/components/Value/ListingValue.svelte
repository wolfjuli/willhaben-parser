<script lang="ts">
    import type {ListingValueProps} from "$lib/components/Value/ListingValue";
    import {listingAttribute} from "$lib/utils/jsonpath.js";
    import Stars from "$lib/components/Stars.svelte";

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

</script>
<span ondblclick={() => ondblclick(listingAttribute(listing, attribute.attribute), listing)}>
{#if attribute.dataType === "LINK" }
    {#if obj && obj.href}
        <a href={obj.href} target="_blank">{obj.value}</a>
    {:else}
        <a href={configuration.listingsBaseUrl + `/${val}`} target="_blank">{val}</a>
    {/if}
{:else if attribute.dataType === "IMAGE"}
    <img src={configuration.imageBaseUrl + `/${val}`} alt={val} />
{:else if attribute.dataType === "RATING" }
    <Stars value={+(userVal ?? val)} maxValue={5} onchange={(newRating) => onclick(newRating, listing)}/>
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
