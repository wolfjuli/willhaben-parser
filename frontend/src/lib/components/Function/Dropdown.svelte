<script lang="ts">
    import type {FunctionDropdownProps} from "$lib/components/Function/Function";

    let {functions, onchange}: FunctionDropdownProps = $props()
    let funs = $derived(Object.values(functions).toSorted((a, b) => a.name.localeCompare(b.name)))

    function changed (ev){
        const fid = ev.explicitOriginalTarget.value
        if (fid === -1) return
        onchange(functions[fid])
    }

</script>

<select onchange={changed}>
    <option value={-1}>Add a Function...</option>

    {#each funs as fun }
        <option value={fun.id}>{fun.name}</option>
    {/each}
</select>
