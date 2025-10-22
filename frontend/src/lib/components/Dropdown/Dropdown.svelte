<script generics="T extends IDObject" lang="ts">

    import type {DropdownProps} from "$lib/components/Dropdown/Dropdown";
    import type {ChangeEventHandler} from "svelte/elements";


    let {
        onchange = () => false, values,
        nameSelector = (v) => v.id,
        emptyFirstLineText = "Select...",
        preSelected
    }: DropdownProps<T> = $props()

    let elem: HTMLSelectElement

    function changed(ev: ChangeEventHandler<HTMLSelectElement>) {
        const v = values.find(v => v.id == ev.target.value)!!
        const reset = onchange(v)
        if (reset)
            elem.selectedIndex = -1
    }

</script>


<select bind:this={elem} onchange={changed}>
    {#if emptyFirstLineText !== undefined}
        <option disabled selected={!elem || elem?.selectedIndex === -1}>{emptyFirstLineText}</option>
    {/if}
    {#each values.toSorted((a, b) => nameSelector(a).localeCompare(nameSelector(b))) as v}
        <option value={v.id} selected={v.id === preSelected}>{nameSelector(v)}</option>
    {/each}
</select>
