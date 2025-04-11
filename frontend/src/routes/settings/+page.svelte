<script lang=ts>
    import type {PageProps} from "./$types";
    import {ListingsStore} from "$lib/stores/ListingsStore.svelte.js";
    import ListingTable from "$lib/components/ListingTable/ListingTable.svelte";
    import {filteredAttributes, mergedAttributes} from "$lib/stores/attributes.svelte";
    import type {Attribute} from "$lib/types/Attribute";
    import {setSettings, settingsStore} from "$lib/stores/settings.svelte";
    import 'bootstrap'
    import 'bootstrap-grid'
    import Dropdown from "$lib/components/Dropdown/Dropdown.svelte";
    import {FunctionsStore} from "$lib/stores/functions.svelte";

    let {data}: PageProps = $props()

    const listings = $derived(ListingsStore.value.listings ?? {})
    const sorting = $derived(ListingsStore.value.sorting ?? [])
    const settings = $derived(settingsStore.value)
    const fields = $derived(filteredAttributes(settings.listingFields))
    const functions = $derived(FunctionsStore.value)
    const attributes = $derived(mergedAttributes().value?.filter(a => !fields.find(f => f.normalized === a.normalized)) ?? [])

    function removeField(field: Attribute) {
        const newSettings = {
            ...settings,
            listingFields: settings.listingFields.filter(l => l !== field.normalized)
        }

        setSettings(newSettings)
    }

    function upOne(field: Attribute, list: string[]): string[] {
        const currIdx = list.indexOf(field.normalized)
        return [...list.slice(0, currIdx - 1), field.normalized, list[currIdx - 1], ...list.slice(currIdx + 1)]
    }

    function up(field: Attribute) {
        const listingFields = upOne(field, settings.listingFields)

        setSettings({
            ...settings,
            listingFields
        })
    }

    function down(field: Attribute) {
        const listingFields = upOne(field, settings.listingFields.reverse()).reverse()
        setSettings({
            ...settings,
            listingFields
        })
    }

    function add(attr: Attribute) {
        const listingFields = [...settings.listingFields, attr.normalized]
        setSettings({
            ...settings,
            listingFields
        })

        return true
    }

    let maxIdx = $derived(fields.length - 1)
</script>

<details>
    <summary>Columns</summary>

    <div class="container-fluid">
    {#each fields as field, idx}
        <div class="row">
            <div class="col-1">{field.label ?? field.normalized}</div>
            <div class="col-4">
                <button onclick={() => up(field)} disabled={idx === 0}>
                    {'<'}
                </button>
                <button onclick={() => removeField(field)}>X</button>
                <button onclick={() => down(field)} disabled={idx === maxIdx}>
                    {'>'}
                </button>

            </div>
        </div>
    {/each}
    <div class="row">
        <Dropdown
            emptyFirstLineText="Add attribute..."
            nameSelector={v => v.label ?? v.normalized}
            onchange={attr => add(attr)}
            values={attributes}
        />
    </div>
</div>

    <ListingTable configuration={data.configuration} {attributes} {fields} {sorting} {listings}/>
</details>

<style>
    .container-fluid .row {
        float: left;
        margin: 0;
        text-align: center;
        margin-right: 1em;
    }

    .container-fluid {
        clear: both;
    }

    details {
        border-bottom: 1px solid var(--color-legend)
    }

    summary {
        height: 2em

    }
</style>
