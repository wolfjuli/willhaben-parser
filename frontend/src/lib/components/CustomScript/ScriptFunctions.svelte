<script lang="ts" nonce="nonce-1vRgYShvB7PTBmm1fZ1pSw==">

    import type {ScriptFunctionsProps} from "$lib/components/CustomScript/Script";
    import FunctionValue from "$lib/components/Function/FunctionValue.svelte";
    import { ScriptsStore} from "$lib/stores/ScriptsStore.svelte.js";
    import type {FunctionDef} from "$lib/types/Function";
    import type {ScriptFunctionDef} from "$lib/types/Script";
    import Dropdown from "$lib/components/Dropdown/Dropdown.svelte";
    import {transformListing} from "$lib/utils/transformListing.js";

    let {script, listing, attributes, functions}: ScriptFunctionsProps = $props()

    let attribute = $derived(attributes.find(a => a.id === script.attributeId)!!)
    let attributeValue = $derived((listing ? transformListing(listing, attributes, functions) : undefined)?.[attribute.normalized]?.base)

    let functionValues = $derived(script
        .functions
        ?.reduce((acc, fId) => {
            const fun = functions[fId.functionId]

            if (!fun)
                return acc

            let exec = undefined
            let value = undefined
            try {
                exec = fun?.function ? eval(fun.function) : undefined
                value  = listing && exec ? exec(acc[acc.length - 1].value!!, listing) : []
            } catch (e) {
                console.error(`Error during execution of function '${fun.name}' on '${attribute.normalized}'`, e)
            }

            return [
                ...acc,
                {
                    scriptId: script.id,
                    functionId: fun.id,
                    ord: fId.ord,
                    name: fun.name,
                    value
                }
            ]
        }, [{
            name: attribute?.normalized,
            value: attributeValue,
            ord: -1,
            functionId: -1,
            scriptId: -1
        }]) ?? []
    )

    function addFunction(fun: FunctionDef) {

        let ord = script.functions[script.functions.length - 1].ord + 1
        let newScript: ScriptFunctionDef = {scriptId: script.id, functionId: fun.id, ord}

        ScriptsStore.createScriptFunction(newScript)
    }

    function removeFunction(fun: ScriptFunctionDef) {
        ScriptsStore.deleteScriptFunction(fun)
    }

</script>


<details>
    <summary>
        Function steps
        {#if listing && functionValues }({functionValues[functionValues.length - 1].value} Points){/if}
    </summary>

    <div class="text-center">
        <FunctionValue {...functionValues[0]}/>
        {#each functionValues.slice(1) as fv}
            ↓ <br/>
            <button onclick={() => removeFunction(fv)}>X</button>
            <FunctionValue {...fv}/>
            <br/>
        {/each}
        <Dropdown nameSelector={(e) => e.name} onchange={addFunction} values={Object.values(functions)}></Dropdown>
    </div>
</details>
