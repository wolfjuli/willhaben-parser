<script lang="ts">
    import type {LayoutProps} from "./$types";
    import {Scheme} from '$lib/colorScheme.svelte'
    import GearIcon from '$lib/assets/images/gear.svg?component'
    import LoadingIcon from '$lib/assets/images/loading.svg?component'
    import '@picocss/pico'
    import 'bootstrap'
    import 'bootstrap-grid'
    import {initializer} from "$lib/stores/initializeStores.svelte";
    import {ListingsStore} from "$lib/stores/ListingsStore.svelte";
    import {ScriptsStore} from "$lib/stores/ScriptsStore.svelte";
    import {FetchingStore} from "$lib/stores/FetchingStore.svelte";

    let {children}: LayoutProps = $props()

    const SCHEME_NAMES = {fromdark: '🌝', fromlight: '🌚'}
    const scheme = new Scheme()

    //Init all singletons
    ListingsStore.instance
    ScriptsStore.instance

    navigator && navigator.storage && navigator.storage.persist()
    initializer.initialize()
</script>

<svelte:head>
    <link rel="stylesheet" href={`${scheme.current}.css`}/>
</svelte:head>

<nav>
    <ul>
        <li><strong>WillHaben Parser</strong></li>
        <li><a href="/settings">
            {#if FetchingStore.fetching}
                <LoadingIcon/>
            {:else}
                <GearIcon/>
            {/if}
        </a></li>
    </ul>
    <ul>
        <li><a href="/">Home</a></li>
        <li><a href="/functions">Functions</a></li>
        <li><a href="/attributes">Attributes</a></li>
    </ul>
</nav>

<main class="container-fluid">
    {@render children()}
</main>


<style>
    button {
        font-size: 30px;
    }

    nav {
        background-color: var(--md-sys-color-on-primary-fixed)
    }
</style>
