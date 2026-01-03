<script lang="ts">
    import type {LayoutProps} from "./$types";
    import {Scheme} from '$lib/colorScheme.svelte'
    import '@picocss/pico'
    import 'bootstrap'
    import 'bootstrap-grid'
    import {ListingsStore} from "$lib/stores/ListingsStore.svelte";
    import {ScriptsStore} from "$lib/stores/ScriptsStore.svelte";
    import {SortingStore} from "$lib/stores/SortingStore.svelte";
    import {SearchParamsStore} from "$lib/stores/SearchParamsStore.svelte";
    import {BaseAttributesStore} from "$lib/stores/Attributes.svelte";
    import Navigation from "$lib/components/Navigation/Navigation.svelte";

    let {children}: LayoutProps = $props()

    const SCHEME_NAMES = {fromdark: '🌝', fromlight: '🌚'}
    const scheme = new Scheme()

    //Init all singletons
    SearchParamsStore.instance
    SortingStore.instance
    ListingsStore.instance
    ScriptsStore.instance
    BaseAttributesStore.instance

    navigator && navigator.storage && navigator.storage.persist()

</script>

<svelte:head>
    <link rel="stylesheet" href={`${scheme.current}.css`}/>
</svelte:head>

<Navigation />

<main class="container-fluid">


    {@render children()}
</main>


