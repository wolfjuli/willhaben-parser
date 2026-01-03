<script lang="ts">

    import type {Snippet} from 'svelte';

    let {
        title = undefined,
        body = undefined,
        children = undefined,
        footer = undefined,
        onclose = undefined,
        open = $bindable(false)
    }: {
        title: Snippet | string | undefined,
        body: Snippet | undefined,
        children: Snippet | undefined,
        footer: Snippet | undefined,
        onclose: (() => boolean) | undefined,
        open: boolean
    } = $props()

    function close(ev) {
        let curr = ev.target
        if (curr.id !== "modal-close-button")
            while (curr.tagName) {
                if (curr.tagName.toLowerCase() === "i-modal")
                    return
                curr = curr.parentNode
            }

        if (!onclose || onclose())
            open = false
    }

</script>

{#if open}
    <i-modal-background>
        <i-modal>
            <i-modal-head>
                <i-modal-title>
                    {#if typeof title === "string" }
                        <h1>{title}</h1>
                    {:else if title}
                        {@render title?.()}
                    {/if}
                </i-modal-title>
                <i-modal-buttons>
                    <button onclick={close} id="modal-close-button">X</button>
                </i-modal-buttons>
            </i-modal-head>
            <i-modal-body>
                {#if body}
                    {@render body()}
                {:else }
                    {@render children?.()}
                {/if}
            </i-modal-body>
            <i-modal-footer>
                {@render footer?.()}
            </i-modal-footer>
        </i-modal>
    </i-modal-background>
{/if}
<style>
    i-modal-background {
        display: block;
        position: absolute;
        left: 0;
        top: 0;
        z-index: 100;
        width: 100%;
        height: 100%;

        background: rgba(0, 0, 0, 0.5)
    }

    i-modal {
        display: flex;
        flex-direction: column;
        position: absolute;
        left: 10vw;
        top: 10vh;
        width: 80vw;
        height: 80vh;
        z-index: 101;
        border-radius: 1em;

        background-color: var(--md-sys-color-primary);
        color: var(--md-sys-color-on-primary);

        padding: 1em;
    }

    i-modal-head {
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        align-items: center;
    }

    i-modal-head i-modal-title {
        align-content: center;
    }

    i-modal-head i-modal-buttons {
        min-width: 2em;
    }

    i-modal-body {
        overflow: scroll;
        flex-grow: 1
    }
</style>