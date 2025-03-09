<script lang="ts">

    import type {ValueProps} from "$lib/components/Value/Value";

    let {value, name}: ValueProps = $props()

</script>

<div>
    <span>{value ?? ""}</span>
    <small>{name}</small>
</div>

<style>
    span {
        display: block
    }
</style>
