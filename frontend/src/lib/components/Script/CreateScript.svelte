<script lang="ts">
    import type {CreateScriptProps} from "$lib/components/Script/Script";
    import {createScript, updateScript} from "$lib/stores/scripts.svelte";
    import type {ScriptSetDef} from "$lib/types/Script";
    import {randomName} from "$lib/utils/names";

    let {attributes}: CreateScriptProps = $props()

    function newScript(): ScriptSetDef {
        return {
            id: -1,
            name: randomName(),
            attributeId: -1
        }
    }

    function save() {
        createScript(script)
        script = newScript()
    }

    let script: ScriptSetDef = $state(newScript())
</script>

<div id={`fun${script.id ?? '-new'}`} class="grid">
    <div>
        New Script on attribute:
    </div>
    <div>
        <select onchange={ev => { script.attributeId = ev.target.value; save()}}>
            {#each attributes as attribute}
                <option value={attribute.id}>{attribute.normalized}</option>
            {/each}
        </select>
    </div>

</div>

<style>
    textarea {
        width: 30vw;
        height: 10vh
    }

</style>
