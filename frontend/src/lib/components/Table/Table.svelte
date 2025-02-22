<script lang="ts">
	import type { TableProps } from './Table'

	let props: TableProps = $props()

	let tableData = props.tableData

	let columnNames: string[] = $derived(Object.keys(props).filter((n) => n !== 'tableData'))
</script>

<table>
	<thead>
		<tr>
			{#each columnNames as columnName}
				<th>{columnName}</th>
			{/each}
		</tr>
	</thead>
	<tbody>
		{#each tableData as obj}
			<tr>
				{#each columnNames as columnName}
					{#if typeof props[columnName] === 'function'}
						<td>{@render props[columnName](obj[columnName], obj)}</td>
					{:else}
						<td>{obj[columnName]}</td>
					{/if}
				{/each}
			</tr>
		{/each}
	</tbody>
</table>
