<script lang="ts">

    import type {EditListingValueProps} from "$lib/components/Value/ListingValue";
    import {listingAttribute} from "$lib/utils/jsonpath";

    let {listing, attribute, onupdate}: EditListingValueProps = $props()

    let value = listingAttribute(listing, attribute.attribute)

</script>

<input onkeyup={(ev: KeyboardEvent) => {
                if(ev.key === "Enter") onupdate(ev.target?.value, listing, attribute)
            }
        }
       type=text
       value={value?.user ?? value?.custom ?? value?.base}
/>
