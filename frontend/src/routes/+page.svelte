<script lang="ts">
    import type {PageProps} from "./$types";
    import ListingTable from "./ListingTable.svelte";

    let {data}: PageProps = $props()

</script>

<ListingTable  configuration={data.configuration} />
