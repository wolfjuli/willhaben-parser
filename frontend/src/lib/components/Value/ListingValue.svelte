<script lang="ts">
    import type {ListingValueProps} from "$lib/components/Value/ListingValue";
    import {isLink} from "$lib/types/Links";

    let {
        listing, attribute, configuration, userListing = undefined,
        onclick = () => {
        },
        ondblclick = () => {
        }
    }: ListingValueProps = $props()

    let val = $derived(listing[attribute.normalized]?.toString() ?? "[empty]")
    let obj = $derived(listing[attribute.normalized] as unknown as { href: string, value: string })
    let userVal = $derived(userListing?.[attribute.normalized] != obj ? userListing?.[attribute.normalized] : undefined)
</script>
<span onclick={() => onclick(listing[attribute.normalized], listing)}
      ondblclick={() => ondblclick(listing[attribute.normalized], listing)}>
{#if attribute.dataType === "LINK" }
    {#if isLink(obj) }
        <a href={configuration.listingsBaseUrl + '/' + obj.href} target="_blank">{obj.value}</a>

    {:else}
        <a href={configuration.listingsBaseUrl + `/${val}`} target="_blank">{val}</a>
    {/if}
{:else if attribute.dataType === "IMAGE"}
    <img src={configuration.imageBaseUrl + `/${val}`} alt={val}/>
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
</style>
