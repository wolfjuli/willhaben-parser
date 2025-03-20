<script lang="ts">

    import type {EditListingValueProps} from "$lib/components/Value/ListingValue";

    let {userListing, listing, attribute, oncreate, onupdate}: EditListingValueProps = $props()

</script>


<input onkeyup={(ev: KeyboardEvent) => {
            if(ev.key === "Enter"){
                if(userListing?.[attribute.normalized])
                    onupdate(ev.target.value, listing, attribute)
                else
                    oncreate(ev.target.value, listing, attribute)}
            }
            } type=text
       value={userListing?.[attribute.normalized] ?? listing[attribute.normalized]}
/>
