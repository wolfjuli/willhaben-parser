<script lang="ts">
    import {Listing} from "../types/Listing";
    import {listings} from "../stores/Listings";
    import {logDebug} from "../modules/Extensions";
    import Map from "./Map.svelte";

    export let id: string = ""

    let _listing: Listing
    listings.subscribe(ls => {
        _listing = ls.find(l => l.id == id)
        logDebug(id, _listing)
    }, false)

    let skipProperties = new Set(["raw", "DISTANCES", "COORDINATES"])
</script>


{#if _listing}
    {#if Object.keys(_listing.attributes).includes("COORDINATES") }
        <Map latLong={_listing.attributes["COORDINATES"][0]}></Map>
    {/if}
    <table class="table table-striped table-sm table-hover">
        <thead>
        <tr>
            <th>Property</th>
            <th>Value</th>
        </tr>
        </thead>
        <tbody>
        {#each Object.keys(_listing.attributes) as key}
            {#if !skipProperties.has(key) }
                <tr>
                    <td>{key}</td>
                    <td>{@html _listing.attributes[key]}</td>
                </tr>
            {/if}
        {/each}
        </tbody>
    </table>
{/if}