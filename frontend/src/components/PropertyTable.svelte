<script lang="ts">

    export let object: {}
    export let skipProperties: string[] = []

</script>

<table class="table table-striped table-sm table-hover">
    <thead>
    <tr>
        <th>Property</th>
        <th>Value</th>
    </tr>
    </thead>
    <tbody>
    {#each Object.keys(object) as key}
        {#if !skipProperties.includes(key) }
            <tr>
                <td>{key}</td>
                <td>{@html object[key]}</td>
            </tr>
        {/if}
    {/each}
    </tbody>
</table>