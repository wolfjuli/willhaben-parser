<script lang="ts">
    let {children} = $props()   
	import { Scheme } from '$lib/colorScheme.svelte'
	const SCHEME_NAMES = { fromdark: '🌝', fromlight: '🌚' }

	const scheme = new Scheme()
</script>

<svelte:head>
	<link rel="stylesheet" href={`${scheme.current}.css`} />
</svelte:head>

<h1>
	WillHaben Parser<small
		><button onclick={() => scheme.switch()}>{SCHEME_NAMES[`from${scheme.current}`]}</button
		></small
	>
</h1>

<style>
	button {
		font-size: 30px;
	}
</style>


{@render children()}
