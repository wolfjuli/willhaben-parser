<script lang="ts">

    import type {EditListingValueProps} from "$lib/components/Value/ListingValue";

    let {listing, attribute, oncreate, onupdate}: EditListingValueProps = $props()

</script>


<input onkeyup={(ev: KeyboardEvent) => {
            if(ev.key === "Enter"){
                if(listing?.[attribute.normalized]?.user)
                    onupdate(ev.target?.value, listing, attribute)
                else
                    oncreate(ev.target?.value, listing, attribute)}
            }
            }
       type=text
       value={listing?.[attribute.normalized]?.user ?? listing[attribute.normalized]?.custom ?? listing[attribute.normalized]?.base}
/>
