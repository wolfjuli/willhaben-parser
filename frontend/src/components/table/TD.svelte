<script lang="ts">

    export let content: string
    export let reducable = content && content.length > 20

    let partValue: string = content.substring(0, 20)

    let showFull = !reducable

    if(content.length > 20){
        reducable = true
        content = partValue
    }

</script>

<td>
    {showFull ? content : partValue}

    {#if reducable}
        <a href="#" on:click={() => showFull = !showFull}>
            {#if showFull}
                Less
            {:else }
                More...
            {/if}
        </a>
    {/if}
</td>