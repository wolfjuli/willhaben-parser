<script lang="ts">

    import type {PageProps} from "./$types";

    import {FunctionsStore} from "$lib/stores/functions.svelte";
    import {ScriptsStore} from "$lib/stores/ScriptsStore.svelte.js";
    import Function from "$lib/components/Function/Function.svelte";
    import CustomScript from "$lib/components/CustomScript/CustomScript.svelte";
    import CreateScript from "$lib/components/CustomScript/CreateCustomScript.svelte";
    import ScriptFunctions from "$lib/components/CustomScript/ScriptFunctions.svelte";
    import ListingSearch from "$lib/components/ListingSearch/ListingSearch.svelte";
    import type {Listing} from "$lib/types/Listing";
    import CreateFunction from "$lib/components/Function/CreateFunction.svelte";
    import ListingDetail from "$lib/components/ListingDetail/ListingDetail.svelte";
    import {mergedAttributes} from "$lib/stores/attributes.svelte";
    import {ListingsStore} from "$lib/stores/ListingsStore.svelte";


    const functions = $derived(FunctionsStore.value)
    const scripts = $derived(ScriptsStore.value)
    const attributes = $derived((mergedAttributes().value?.toSorted((a, b) => a.normalized.localeCompare(b.normalized)) ?? []))
    const listings = $derived(ListingsStore.value.listings ?? {})
    const sorted = $derived(ListingsStore.instance.filtered  ?? [])

    let selectedListing = $state<Listing | undefined>(undefined)

    let {data}: PageProps = $props()
    let configuration = $derived(data.configuration)

</script>

<div class="grid">
    <div>
        {#if scripts}
            <h3>Scripts</h3>
            <CreateScript {attributes}/>
            <hr/>
            {#each Object.keys(scripts) as id }
                <CustomScript script={scripts[id]} {attributes}/>
                <ScriptFunctions script={scripts[id]} {attributes} {functions} listing={selectedListing}/>
                <hr/>
            {/each}
        {/if}
    </div>
    <div>
        <h3>Example Listing</h3>
        <ListingSearch {listings}
                       {sorted}
                       onselect={(sel:Listing | undefined ) => { selectedListing = sel}}/>

        {#if selectedListing}
            <ListingDetail listing={selectedListing} {attributes} {configuration} ></ListingDetail>
        {/if}
    </div>
</div>
<div class="grid">
    <div>
        {#if functions}
            <h3>Functions</h3>
            <CreateFunction/>
            <hr/>
            {#each Object.keys(functions) as id }
                <Function fun={functions[id]}/>
            {/each}
        {/if}
    </div>
</div>
