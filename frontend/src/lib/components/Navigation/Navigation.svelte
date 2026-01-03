<script lang="ts">
import {FetchingStore} from "$lib/stores/FetchingStore.svelte.js";
import LoadingIcon from "$images/loading.svg";
import GearIcon from "$images/gear.svg";
</script>

<nav>
    <ul>
        <li><strong>WillHaben Parser</strong></li>
        <li><a href="/settings">
            {#if FetchingStore.fetching}
                <LoadingIcon/>
            {:else}
                <GearIcon/>
            {/if}
        </a></li>
    </ul>
    <ul>
        <li><a href="/">Home</a></li>
        <li><a href="/functions">Functions</a></li>
        <li><a href="/attributes">Attributes</a></li>
    </ul>
</nav>


<style>
    button {
        font-size: 30px;
    }

    nav {
        background-color: var(--md-sys-color-on-primary-fixed)
    }
</style>