<script lang="ts">
    import {createFunction} from "$lib/stores/functions.svelte";

    function save(name: string) {
        createFunction({id: -1, name, function: "(val, row) => val"})

    }

</script>

<div class="grid">
    <div>
            <input
                type="text"
                placeholder="New Function..."
                onkeyup= {(ev: KeyboardEvent) => { console.log(ev); if(ev.key === "Enter") {  save(ev.target!!.value); ev.target.value = ""}} }
            />

    </div>

</div>
