<script lang="ts">
	import GoogleMap from '$lib/components/Map/GoogleMap.svelte'
	import Table from '$lib/components/Table/Table.svelte'

	let tableData= [
		{
			a: 1,
			b: "Hello",
			c: "nope"
		},
		{
			a: 2,
			b: "World",
			c: "Still nopw"
		}
	]
</script>

<Table {tableData} a="" >
	{#snippet b(val, obj)}
		Render it with addition text: {val}
	{/snippet}
</Table>