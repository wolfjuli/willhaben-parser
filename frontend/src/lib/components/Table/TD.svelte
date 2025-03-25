<script lang="ts" generics="T extends Record<string, unknown>">
    import {type Snippet} from 'svelte'

    type PropsType = {
        colspan: number,
        property: string ,
        row: T ,
        right: boolean ,
        formatter: ((value: number) => string) | undefined ,
        onclick: (() => void) | undefined,
        render: Snippet
    }

    let {
        colspan = 1,
        right = false,
        onclick = undefined,
        render
    } : PropsType = $props()

</script>

<!-- svelte-ignore a11y_no_static_element_interactions -->
<td {colspan} class:right>
    <svelte:element this={onclick ? 'button' : 'div'} {onclick} >
        {@render render()}
    </svelte:element>
</td>

<style>
    td {
        font-size: 0.6em;
    }
    /*
    td {
        border-bottom: 1px solid var(--md-sys-color-surface-container-high);
        text-align: left;
    }

    td.right {
        text-align: right;
    }
    td > button,
    td > div {
        padding: 0.5rem;
    }

    button {
        all: unset;
        cursor: pointer;
        box-sizing: border-box;
        width: 100%;
    }

    button:focus-visible {
        outline: 3px solid var(--md-sys-color-primary);
        outline-offset: -3px;
    }

     */
</style>

