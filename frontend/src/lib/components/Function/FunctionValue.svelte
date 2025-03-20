<script lang="ts">

    import type {ValueProps} from "$lib/components/Function/Function";

    let {value, name}: ValueProps = $props()

</script>

<div>
    <span>{name}</span>
    <small>{value ?? ""}</small>
</div>

<style>
    span {
        display: block
    }
</style>
