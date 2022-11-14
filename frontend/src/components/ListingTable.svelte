<script lang="ts">
    import {Listing} from "../types/Listing";
    import {listings} from "../stores/Listings";

    export let id: string = ""

    let _listings: Listing[]

    listings.subscribe(ls => _listings = ls)



</script>

<table class="table table-striped table-sm table-hover">
    <thead>
    <tr>
        <th>Property</th>
        <th>Value</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td></td>
    </tr>
    </tbody>
</table>