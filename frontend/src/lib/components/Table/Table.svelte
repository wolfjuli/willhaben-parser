<script generics="T" lang="ts">
    import type {TableProps} from './Table'

    let {tableData, thead, row}: TableProps<T> = $props()
</script>

<table class="striped">
	{#if thead}
		<thead>
			{@render thead()}
		</thead>
	{/if}
	<tbody>
		{#each tableData as obj, idx}
			{@render row(obj, idx)}
		{/each}
	</tbody>
</table>
