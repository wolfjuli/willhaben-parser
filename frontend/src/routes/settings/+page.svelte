<script lang=ts>
    import type {PageProps} from "./$types";
    import ListingTable from "$lib/components/ListingTable/ListingTable.svelte";
    import {BaseAttributesStore} from "$lib/stores/Attributes.svelte.js";
    import type {Attribute, BaseAttribute} from "$lib/types/Attribute";
    import {setSettings, settingsStore} from "$lib/stores/settings.svelte";
    import 'bootstrap'
    import 'bootstrap-grid'
    import Dropdown from "$lib/components/Dropdown/Dropdown.svelte";
    import {SortingStore} from "$lib/stores/SortingStore.svelte";
    import Input from "$lib/components/Input/Input.svelte";

    let {data}: PageProps = $props()


    const sorting = $derived(SortingStore.value.sorting ?? [])
    const settings = $derived(settingsStore.value)
    const fields = $derived(settings.listingFields.map(f => BaseAttributesStore.value?.find(a => f === a.attribute)).filter(Boolean))
    const attributes = $derived(BaseAttributesStore.value?.filter(a => !fields.find(f => f!!.attribute === a.attribute)) ?? [])

    function removeField(field: Attribute) {
        const newSettings = {
            ...settings,
            listingFields: settings.listingFields.filter(l => l !== field.attribute)
        }

        setSettings(newSettings)
    }

    function upOne(field: Attribute, list: string[]): string[] {
        const currIdx = list.indexOf(field.attribute)
        return [...list.slice(0, currIdx - 1), field.attribute, list[currIdx - 1], ...list.slice(currIdx + 1)]
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
        const listingFields = [...settings.listingFields, attr.attribute]
        setSettings({
            ...settings,
            listingFields
        })

        return true
    }

    let maxIdx = $derived(fields.length - 1)

    function attrDataType(field: BaseAttribute, dataType: { id: string, name: string }): boolean {
        field.dataType = dataType.id
        BaseAttributesStore.updateAttribute(field)

        return false
    }

    function attrLabel(field: BaseAttribute, newName: string) {
        field.label = newName
        BaseAttributesStore.updateAttribute(field)

        editing.fieldId = -1
    }

    let editing = $state({fieldId: -1})

</script>

<details>
    <summary>Columns</summary>

    <div class="container-fluid">
        {#each fields as field, idx}
            {#if field}
                <div class="row">
                    <div class="col-1" ondblclick={() => editing.fieldId = field.id}>
                        {#if editing.fieldId === field.id}
                            <Input onsubmit={val => attrLabel(field, val)} value={field.label ?? field.attribute}
                                   placeholder={"Label"}></Input>
                        {:else}
                            {field.label ?? field.attribute}
                        {/if}
                    </div>

                    <div class="col-1">
                        <Dropdown
                            emptyFirstLineText="Type"
                            nameSelector={e => e.name}
                            onchange={type => { attrDataType(field, type) }}
                            values={["TEXT", "IMAGE", "LINK"].map (t => ({id: t, name: t}))}
                            preSelected={field.dataType}
                        />
                    </div>
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
            {/if}
        {/each}
        <div class="row">
            <Dropdown
                emptyFirstLineText="Add attribute..."
                nameSelector={v => v.label ?? v.attribute ?? '[unknown]'}
                onchange={attr => add(attr)}
                values={attributes}
            />
        </div>
    </div>

    <ListingTable {attributes} configuration={data.configuration} {fields} {sorting}/>
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
