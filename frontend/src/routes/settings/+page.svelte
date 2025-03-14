<script lang=ts>
    import type {PageProps} from "./$types";
    import {ListingsStore} from "$lib/stores/listings.svelte";
    import ListingTable from "$lib/components/ListingTable/ListingTable.svelte";
    import {AttributesStore, filteredAttributes} from "$lib/stores/attributes.svelte";
    import type {Attribute} from "$lib/types/Attribute";
    import {setSettings, settingsStore} from "$lib/stores/settings.svelte";
    import 'bootstrap'
    import 'bootstrap-grid'
    import Dropdown from "$lib/components/Dropdown/Dropdown.svelte";

    let {data}: PageProps = $props()

    let listings = $derived(ListingsStore.value ?? [])
    const settings = $derived(settingsStore.value)
    const fields = $derived(filteredAttributes(settings.listingFields))
    const attributes = $derived(AttributesStore.value?.filter(a => !fields.find(f => f.normalized === a.normalized)) ?? [])

    function removeField(field: Attribute) {
        console.log("remove", field)
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
        console.log(settings.listingFields, listingFields)

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
        console.log(listingFields)
        setSettings({
            ...settings,
            listingFields
        })

        return true
    }

    let maxIdx = $derived(fields.length - 1)
</script>


<div class="container">
    {#each fields as field, idx}
        <div class="row">
            <div class="col-1">{field.label ?? field.normalized}</div>
            <div class="col-4">
                <button onclick={() => removeField(field)}>X</button>
                <button onclick={() => up(field)} disabled={idx === 0}>
                    ^
                </button>
                <button onclick={() => down(field)} disabled={idx === maxIdx}>
                    v
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

<ListingTable configuration={data.configuration} {fields} {listings}/>

