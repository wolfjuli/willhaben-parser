<script lang="ts" nonce="nonce-1vRgYShvB7PTBmm1fZ1pSw==">

    import type {ScriptFunctionsProps} from "$lib/components/Script/Script";
    import Value from "$lib/components/Value/Value.svelte";
    import {createScriptFunction, deleteScriptFunction, updateScript} from "$lib/stores/scripts.svelte";
    import Dropdown from "$lib/components/Function/Dropdown.svelte";
    import type {FunctionDef} from "$lib/types/Function";
    import type {ScriptFunctionDef} from "$lib/types/Script";

    let {script, listing, attributes, functions}: ScriptFunctionsProps = $props()

    let attribute = $derived(attributes.find(a => a.id === script.attributeId)!!)
    let attributeValue = $derived(listing?.[attribute.normalized])

    let functionValues = $derived(script
        .functions
        ?.reduce((acc, fId) => {
            const fun = functions[fId.functionId]
            const exec = fun?.function ? eval(fun.function) : undefined
            if (!fun)
                return acc

            return [
                ...acc,
                {
                    scriptId: script.id,
                    functionId: fun.id,
                    ord: fId.ord,
                    name: fun.name,
                    value: listing && exec ? exec(acc[acc.length - 1].value!!, listing) : [],
                }
            ]
        }, [{
            name: attribute.normalized,
            value: attributeValue,
            ord: -1,
            functionId: -1,
            scriptId: -1
        }]) ?? []
    )

    function addFunction(fun: FunctionDef) {

        let ord = script.functions[script.functions.length - 1].ord + 1
        let newScript: ScriptFunctionDef = {scriptId: script.id, functionId: fun.id, ord}

        createScriptFunction(newScript)
    }

    function removeFunction(fun: ScriptFunctionDef) {
        deleteScriptFunction(fun)
    }

    $effect(() => console.log("Current Script", script))

</script>


<details>
<summary>
    Function steps {#if listing }- {functionValues[functionValues.length - 1].value} Points {/if}
</summary>

<div class="text-center">
    <Value {...functionValues[0]}/>
    {#each functionValues.slice(1) as fv}
        ↓ <br/>
        <Value {...fv}/>
        <button onclick={() => removeFunction(fv)}>X</button>
    {/each}
    <Dropdown {functions} onchange={addFunction}></Dropdown>
</div>
</details>
