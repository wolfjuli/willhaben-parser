<script lang="ts">
    let {children} = $props()
    import {Scheme} from '$lib/colorScheme.svelte'
    import '@picocss/pico'

    const SCHEME_NAMES = {fromdark: '🌝', fromlight: '🌚'}

    const scheme = new Scheme()
</script>

<svelte:head>
    <link rel="stylesheet" href={`${scheme.current}.css`}/>
</svelte:head>

<main class="container">
    <nav>
        <ul>
            <li><strong>WillHaben Parser</strong></li>
        </ul>
        <ul>
            <li><a href="/">Home</a></li>
            <li><a href="/functions">Functions</a></li>
        </ul>
    </nav>

</main>

<style>
    button {
        font-size: 30px;
    }
</style>


{@render children()}
