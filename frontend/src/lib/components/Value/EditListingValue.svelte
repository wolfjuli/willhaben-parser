<script lang="ts">

    import type {EditListingValueProps} from "$lib/components/Value/ListingValue";
    import {listingAttribute} from "$lib/utils/jsonpath";

    let {listing, attribute, oncreate, onupdate}: EditListingValueProps = $props()

    let value = listingAttribute(listing, attribute.attribute)

</script>

<input onkeyup={(ev: KeyboardEvent) => {
            if(ev.key === "Enter"){
                if(value?.user)
                    onupdate(ev.target?.value, listing, attribute)
                else
                    oncreate(ev.target?.value, listing, attribute)}
            }
            }
       type=text
       value={value?.user ?? value?.custom ?? value?.base}
/>
