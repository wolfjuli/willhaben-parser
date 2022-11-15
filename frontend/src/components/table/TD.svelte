<script lang="ts">

    export let title: string = ""
    export let expandable: false = $$slots.default
    export let showFull: boolean = false


</script>

<td>
    {@html title}
    {#if expandable}
        <a on:click={() => showFull = !showFull}>
            {#if showFull}
                Less
            {:else }
                More...
            {/if}
        </a>
    {/if}
    {#if showFull}
        <slot></slot>
        <a href="#" on:click={() => showFull = !showFull}> Less </a>
    {/if}
</td>