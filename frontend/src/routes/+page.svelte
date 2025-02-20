<script lang="ts">
	import { Scheme } from '$lib/colorScheme.svelte'
	const SCHEME_NAMES = { fromdark: '🌝', fromlight: '🌚' }

	const scheme = new Scheme()
</script>

<svelte:head>
	<link rel="stylesheet" href={`${scheme.current}.css`} />
</svelte:head>

<h1>Welcome to MobiX</h1>
<button onclick={() => scheme.switch()}>{SCHEME_NAMES[`from${scheme.current}`]}</button>

<style>
	button {
		font-size: 30px;
	}
</style>
