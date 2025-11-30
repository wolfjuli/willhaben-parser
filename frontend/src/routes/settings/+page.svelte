<script lang=ts>
    import type {PageProps} from "./$types";
    import {BaseAttributesStore} from "$lib/stores/Attributes.svelte.js";
    import type {Attribute, BaseAttribute, CreateUserAttribute} from "$lib/types/Attribute";
    import 'bootstrap'
    import 'bootstrap-grid'
    import Dropdown from "$lib/components/Dropdown/Dropdown.svelte";
    import Input from "$lib/components/Input/Input.svelte";
    import {SearchParamsStore} from "$lib/stores/SearchParamsStore.svelte";
    import ListingTable from "../ListingTable.svelte";

    let {data}: PageProps = $props()
    const settings = $derived(SearchParamsStore.value)
    const fields = $derived(settings.viewAttributes.map(f => BaseAttributesStore.value?.find(a => f === a.attribute)).filter(Boolean)) as Attribute[]
    const attributes = $derived(BaseAttributesStore.value?.filter(a => !fields.find(f => f!!.attribute === a.attribute)) ?? [])


    function removeField(field: Attribute) {
        const newSettings = {
            ...settings,
            viewAttributes: settings.viewAttributes.filter(l => l !== field.attribute)
        }

        SearchParamsStore.set(newSettings)
    }

    function upOne(field: Attribute, list: string[]): string[] {
        const currIdx = list.indexOf(field.attribute)
        return [...list.slice(0, currIdx - 1), field.attribute, list[currIdx - 1], ...list.slice(currIdx + 1)]
    }

    function up(field: Attribute) {
        const viewAttributes = upOne(field, settings.viewAttributes)

        SearchParamsStore.set({
            ...settings,
            viewAttributes
        })
    }

    function down(field: Attribute) {
        const viewAttributes = upOne(field, settings.viewAttributes.reverse()).reverse()
        SearchParamsStore.set({
            ...settings,
            viewAttributes
        })
    }

    function add(attr: Attribute) {
        const viewAttributes = [...settings.viewAttributes, attr.attribute]
        SearchParamsStore.set({
            ...settings,
            viewAttributes
        })

        return true
    }

    let maxIdx = $derived(fields.length - 1)

    function attrDataType(field: BaseAttribute, dataType: { id: string, name: string }): boolean {
        field.dataType = dataType.id
        BaseAttributesStore.set(field)

        return false
    }

    function attrSortBy(field: BaseAttribute, sortBy: BaseAttribute): boolean {
        field.sortingAttribute = sortBy.id
        BaseAttributesStore.set(field)

        return false
    }

    function attrLabel(field: BaseAttribute, newName: string) {
        field.label = newName
        BaseAttributesStore.set(field)

        editing.fieldId = -1
    }

    let editing = $state({fieldId: -1})

    $effect(() => {
        if (settings && SearchParamsStore.value) {
            SearchParamsStore.value.viewAttributes = settings.viewAttributes
        }
    })

    function sortField(field: Attribute) {
        return fields.find(f => f.attribute === field.sortingAttribute) ?? field
    }

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
                            values={["TEXT", "NUMBER", "IMAGE", "LINK"].map (t => ({id: t, name: t}))}
                            preSelected={field.dataType}
                        />
                    </div>
                    <div class="col-1">
                        <Dropdown
                            emptyFirstLineText="Sort by..."
                            nameSelector={v => v.label ?? v.attribute}
                            onchange={attr => attrSortBy(field, attr)}
                            values={attributes}
                            preSelected={sortField(field).label ?? sortField(field)?.attribute}
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
                nameSelector={v => v.label ?? v.attribute }
                onchange={attr => add(attr)}
                values={attributes}
            />
        </div>
    </div>

    <ListingTable configuration={data.configuration} />

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
