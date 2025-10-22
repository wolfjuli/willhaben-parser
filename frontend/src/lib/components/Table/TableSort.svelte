<script lang="ts">
    import ArrowDown from '$images/arrow_drop_down.svg?component'
    import ArrowUp from '$images/arrow_drop_up.svg?component'

    export let sorted: boolean
    export let sortAscending: boolean

    $: color = sorted ? undefined : 'gray'
</script>

{#if sortAscending}
    <md-icon>
        <ArrowUp {color} />
        <ArrowDown color="gray" />
    </md-icon>
{:else}
    <md-icon>
        <ArrowUp color="gray" />
        <ArrowDown {color} />
    </md-icon>
{/if}

<style>
    md-icon {
        flex-direction: column;
    }
</style>
