<script lang="ts">

    import {listings} from "../stores/Listings";
    import {attributes} from "../stores/Attributes";
    import {onMount} from "svelte";
    import TD from "./table/TD.svelte";
    import {logDebug} from "../modules/Extensions";

    let _keys = []
    attributes.subscribe(as => _keys = as)

    let _listings = []
    let visibleListings = []
    let visibleTimer = null

    listings.subscribe(ls => {
        _listings = ls
        if (visibleTimer)
            clearInterval(visibleTimer)

        let steps = 20
        visibleListings = _listings.slice(0, steps)

        let vlidx = steps
        visibleTimer = setInterval(() => {
            if (vlidx >= _listings.length)
                return clearTimeout(visibleTimer)

            visibleListings = [...visibleListings, ..._listings.slice(vlidx, vlidx + steps)]
            vlidx += steps

            logDebug(vlidx)
        }, 10)
    })

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
            {#each visibleListings as listing}
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
            {/each}
            </tbody>
        </table>
    {:else}
        <strong> No Data found</strong>
    {/if}
</div>