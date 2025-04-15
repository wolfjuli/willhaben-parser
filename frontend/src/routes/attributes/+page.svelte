<script lang="ts">
    import type {PageProps} from "./$types";
    import ListingFilter from "$lib/components/ListingFilter/ListingFilter.svelte";
    import {FunctionsStore} from "$lib/stores/functions.svelte";
    import {ScriptsStore} from "$lib/stores/ScriptsStore.svelte.js";
    import {CustomAttributesStore, mergedAttributes} from "$lib/stores/attributes.svelte";
    import type {Listing} from "$lib/types/Listing.js";
    import Function from "$lib/components/Function/Function.svelte";

    const functions = $derived(FunctionsStore.value)
    const scripts = $derived(ScriptsStore.value)
    const attributes = $derived((mergedAttributes().value?.toSorted((a, b) => a.normalized.localeCompare(b.normalized)) ?? []))
    const customAttributes = $derived(CustomAttributesStore.value ?? [])

    const {data}: PageProps = $props()

    let filterFun = $state((l: Listing): boolean => true)

    let newAttr = $state({
        id: -1,
        normalized: '',
        label: '',
        dataType: '',
        functionId: -1,
    })

    function add() {

    }
</script>


<ListingFilter {attributes} ></ListingFilter>

{#each customAttributes as attribute}
    <e:attribute>
        <h3>{attribute.label} <small>[{attribute.dataType}]</small></h3>

        <div>
            <Function fun={functions[attribute.functionId]}/>
        </div>
        <hr/>
    </e:attribute>
{/each}

<div role="group">
    <input bind:value={newAttr.normalized} placeholder="Attribute Name" type="text"/>
    <input bind:value={newAttr.label} placeholder="Label" type="text"/>
    <input bind:value={newAttr.dataType} placeholder="Data Type" type="text"/>

    <button onclick={add}>+</button>
</div>
