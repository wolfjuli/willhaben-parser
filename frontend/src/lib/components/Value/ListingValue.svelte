<script lang="ts">
    import type {ListingValueProps} from "$lib/components/Value/ListingValue";
    import {isLink} from "$lib/types/Links";
    import {ListingsStore} from "$lib/stores/ListingsStore.svelte";

    let {
        listing, attribute, configuration,
        onclick = () => {
        },
        ondblclick = () => {
        }
    }: ListingValueProps = $props()

    let attr = $derived(listing && attribute ? listing[attribute.normalized] : undefined)
    let val = $derived(attr?.base?.toString() ?? "[empty]")
    let userVal = $derived(attr?.user?.toString())
    let obj = $derived(attr?.custom as unknown as { href: string, value: string })
</script>
<span onclick={() => onclick(listing[attribute.normalized], listing)}
      ondblclick={() => ondblclick(listing[attribute.normalized], listing)}>
{#if attribute.dataType === "LINK" }
    {#if obj && obj.href}
        <a href={configuration.listingsBaseUrl + '/' + obj.href} target="_blank">{obj.value}</a>
    {:else}
        <a href={configuration.listingsBaseUrl + `/${val}`} target="_blank">{val}</a>
    {/if}
{:else if attribute.dataType === "IMAGE"}
    <img src={configuration.imageBaseUrl + `/${val}`} alt={val}/>
{:else}
    <div>
    <span class:strike={userVal}>
       {#if attribute.normalized === "points" || attribute.normalized === "willhabenId"} {attr} {:else} {val} {/if}
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
