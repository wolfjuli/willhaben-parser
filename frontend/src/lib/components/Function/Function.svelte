<script lang="ts">
    import type {FunctionProps} from "$lib/components/Function/Function";
    import { deleteFunction, updateFunction} from "$lib/stores/functions.svelte";
    import {randomName} from "$lib/utils/names";

    let {fun = {}}: FunctionProps = $props()

    function resetFun() {
        if (fun) {
            if (fun.name === undefined) fun.name = randomName()
            if (!fun.function) fun.function = '(val, row) => val'
        }
    }


    function save() {

        resetFun()
        if (!fun.name)
            deleteFunction(fun)
        else {
            updateFunction(fun)
            fun = {}
            resetFun()
        }
    }

    let editFun = $state(false)
    let editName = $state(false)

    resetFun()
</script>

<div id={`fun${fun.id ?? '-new'}`} class="grid">

    <div>
    {#if editName}
        <input
            type="text"
            placeholder={fun.name}
            bind:value={fun.name}
            onfocusout={() => { editName = false; save()}}
            onfocusin={(ev) => ev.target.select()}
            onkeydown={(ev: KeyboardEvent) => { if(ev.key === "Enter") { editName = false; save()}} }
            autofocus
        />
    {:else }
        <span onclick={() =>  editName = true}>{fun.name}</span>
    {/if}
    </div>
    <div>
    {#if editFun}
        <textarea
            onfocusin={(ev) => ev.target.setSelectionRange(ev.target.value.length,ev.target.value.length)}
            onfocusout={() => {editFun = false; save()}}
            onkeyup={(ev: KeyboardEvent) => {
                      if(ev.altKey && ev.key === "Enter") {
                      editFun = false
                      save()
                      ev.preventDefault()
                      return false
                  }
                  fun.function = ev.target.value
                  }}
            autofocus
        >{fun.function}</textarea>
    {:else }
        <pre onclick={() => editFun = true}>{fun.function}</pre>
    {/if}
    </div>

</div>

<style>
    textarea {
        width: 30vw;
        height: 10vh
    }

</style>
