<script lang="ts">
    import type {CreateScriptProps} from "$lib/components/CustomScript/Script";
    import {createScript} from "$lib/stores/scripts.svelte";
    import type {ScriptSetDef} from "$lib/types/Script";
    import {randomName} from "$lib/utils/names";
    import Dropdown from "$lib/components/Dropdown/Dropdown.svelte";
    import type {Attribute} from "$lib/types/Attribute";

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

    function selected(a: Attribute): boolean {
        script.attributeId = a.id;
        save()
        return true
    }

    let script: ScriptSetDef = $state(newScript())
</script>

<div id={`fun${script.id ?? '-new'}`} class="grid">
    <div>
        New Script on attribute:
    </div>
    <div>
        <Dropdown nameSelector={(a) => a.label} onchange={selected} values={attributes}/>
    </div>

</div>

<style>
    textarea {
        width: 30vw;
        height: 10vh
    }

</style>
