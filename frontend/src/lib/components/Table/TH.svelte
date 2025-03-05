<script lang="ts">
    import TableSort from './TableSort.svelte'

    type PropsType =
        {
        label: string,
        currentColumn: string,
        sorted: boolean,
        sortAscending: boolean,
        onSort: ((key: string) => void) | undefined,
    }


    let {
        label,
        currentColumn,
        sorted = false,
        sortAscending = false,
        onSort = undefined,
    }: PropsType = $props();

    let sortable: boolean = $derived(!!onSort)
    const sortDirection = sortAscending ? 'ascending' : 'descending'
</script>

<th aria-sort={sorted ? sortDirection : undefined}>
    <svelte:element
        this={sortable ? 'button' : 'div'}
        role={sortable ? 'button' : ''}
        onclick={() => onSort?.(currentColumn)}
    >
        <i-th-content>
            {label}
            {#if sortable}
                <TableSort {sorted} {sortAscending} />
            {/if}
        </i-th-content>
    </svelte:element>
</th>

<style>
    i-th-content {
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        align-items: center;
        gap: 1rem;
        padding: 0.5rem;
        font-weight: var(--md-sys-typescale-label-large-font-weight);
        font-size: var(--md-sys-typescale-label-large-font-size);
        line-height: var(--md-sys-typescale-label-large-line-height);
        font-family: var(--md-sys-typescale-label-large-font-family-name);
        letter-spacing: var(--md-sys-typescale-label-large-letter-spacing);
    }

    th > button {
        border: none;
        background-color: transparent;
        padding: 0;
        width: 100%;
    }
</style>
