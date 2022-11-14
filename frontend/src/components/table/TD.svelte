<script lang="ts">

    export let title: string = ""
    export let expandable: false = $$slots.default
    export let showFull: boolean = false


</script>

<td>
    {@html title}
    {#if showFull}
        <slot></slot>
    {/if}
    {#if expandable}
        <a href="#" on:click={() => showFull = !showFull}>
            {#if showFull}
                Less
            {:else }
                More...
            {/if}
        </a>
    {/if}
</td>