<script lang="ts">

    import {listings} from "../stores/Listings";
    import {attributes} from "../stores/Attributes";
    import {onMount} from "svelte";
    import TD from "./table/TD.svelte";

    let _keys = []
    attributes.subscribe(as => _keys = as)

    let _listings = []
    listings.subscribe(ls => _listings = ls)

    onMount(() => {
        attributes.refresh()
        listings.refresh()
    })

</script>

<h1>Listings</h1>

<div class="table-responsive">
    {#if _keys.length > 0}
        <table class="table table-striped table-sm">
            <thead>
            <tr>
                <th>ID</th>
                {#each _keys as head}
                    <th id="{head.id}">{head.name}</th>
                {/each}
            </tr>
            </thead>
            <tbody>
            {#each _listings as listing, idx}
                {#if idx < 10}
                    <tr>
                        <td>
                            <a target="_blank"
                               href="https://www.willhaben.at/iad/object?adId={listing.id}">
                                {listing.id}
                            </a>
                        </td>
                        {#each _keys as key}
                            <TD content={listing.attributes[key.id] ? listing.attributes[key.id].join(" ") : "" }></TD>
                        {/each}
                    </tr>
                {/if}
            {/each}
            </tbody>
        </table>
    {:else}
        <strong> No Data found</strong>
    {/if}
</div>