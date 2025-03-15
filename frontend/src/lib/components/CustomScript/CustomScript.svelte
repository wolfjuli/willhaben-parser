<script lang="ts">
    import {deleteScript, updateScript} from "$lib/stores/scripts.svelte";
    import type {ScriptProps} from "$lib/components/CustomScript/Script";
    import {randomName} from "$lib/utils/names";
    import Dropdown from "$lib/components/Dropdown/Dropdown.svelte";

    let {script, attributes}: ScriptProps = $props()

    function resetScript() {
        script = script || {}
        if (script) {
            if (script.name === undefined) script.name = randomName()
            if (!script.attributeId) script.attributeId = attributes[0]?.id
        }
    }

    function save() {
        resetScript()

        if (!script.name) {
            deleteScript(script)
        } else {
            updateScript(script)
            script = undefined
            resetScript()
        }
    }

    let editName = $state(false)

    resetScript()
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
        <Dropdown nameSelector={a => a.label} onchange={a => {
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
