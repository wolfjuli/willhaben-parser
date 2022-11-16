<script lang="ts">

    import {prevent_default} from "svelte/internal";

    export let title: string = ""
    export let expandable: false = $$slots.default
    export let showFull: boolean = false


    function onClick() {
        return prevent_default(() => showFull = !showFull)
    }

</script>

<td>
    {@html title}
    {#if expandable}
        <a href="#" on:click={onClick()}>
            {#if showFull}
                Less
            {:else }
                More...
            {/if}
        </a>
    {/if}
    {#if showFull}
        <slot></slot>
        <a href="#" on:click={onClick()}> Less </a>
    {/if}
</td>