<script lang="ts">


    import {FunctionsStore} from "$lib/stores/functions.svelte";
    import {ScriptsStore} from "$lib/stores/scripts.svelte";
    import Function from "$lib/components/Function/Function.svelte";
    import Script from "$lib/components/Script/Script.svelte";
    import {AttributesStore} from "$lib/stores/attributes.svelte";
    import CreateScript from "$lib/components/Script/CreateScript.svelte";
    import ScriptFunctions from "$lib/components/Script/ScriptFunctions.svelte";
    import ListingSearch from "$lib/components/ListingSearch/ListingSearch.svelte";
    import {ListingsStore} from "$lib/stores/listings.svelte";
    import type {Listing} from "$lib/types/Listing";
    import CreateFunction from "$lib/components/Function/CreateFunction.svelte";

    const {children} = $props()

    const functions = $derived(FunctionsStore.value)
    const scripts = $derived(ScriptsStore.value)
    const attributes = $derived((AttributesStore.value?.toSorted((a, b) => a.normalized.localeCompare(b.normalized)) ?? []))
    const listings = $derived(ListingsStore.value ?? [])

    let selectedListing = $state<Listing | undefined>(undefined)

    $effect(() => console.log(scripts))
</script>

<div class="grid">
    <div>
        {#if scripts}
            <h3>Scripts</h3>
            <CreateScript {attributes}/>
            <hr/>
            {#each Object.keys(scripts) as id }
                <Script script={scripts[id]} {attributes}/>
                <ScriptFunctions script={scripts[id]} {attributes} {functions} listing={selectedListing}/>
                <hr />
            {/each}
        {/if}
    </div>
    <div>
        <h3>Example Listing</h3>
        <ListingSearch {listings}
                       onselect={(sel:Listing | undefined ) => { selectedListing = sel}}/>
    </div>
</div>
<div class="grid">
    <div>
        {#if functions}
            <h3>Functions</h3>
            <CreateFunction />
            <hr/>
            {#each Object.keys(functions) as id }
                <Function fun={functions[id]}/>
            {/each}
        {/if}
    </div>
</div>
