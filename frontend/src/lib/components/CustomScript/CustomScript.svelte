<script lang="ts">
    import type {ScriptProps} from "$lib/components/CustomScript/Script";
    import Dropdown from "$lib/components/Dropdown/Dropdown.svelte";
    import {ScriptsStore} from "$lib/stores/ScriptsStore.svelte.js";

    let {script, attributes}: ScriptProps = $props()

    function save() {
        if (!script.name) {
            ScriptsStore.delete(script)
        } else {
            ScriptsStore.update(script)
        }
    }

    let editName = $state(false)
</script>

<div class="grid">
    <div>
        {#if editName}
            <input
                type="text"
                placeholder={script.name}
                bind:value={script.name}
                onfocusout={() => { editName = false; save()}}
                onfocusin={(ev) => ev.target.select()}
                onkeydown={(ev: KeyboardEvent) => { if(ev.key === "Enter") { editName = false; save()}} }
                autofocus
            />
        {:else }
            <span onclick={() =>  editName = true}>{script.name}</span>
        {/if}
    </div>
    <div>
        <Dropdown nameSelector={a => a.label ?? a.attribute} onchange={a => {
            script.attributeId = a.id
            save()
            return false
        }} preSelected={script.attributeId} values={attributes}/>
    </div>
</div>

<style>
    textarea {
        width: 30vw;
        height: 10vh
    }
</style>
