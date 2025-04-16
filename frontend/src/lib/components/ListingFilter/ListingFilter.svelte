<script lang="ts">

    import type {ListingFilterProps} from "$lib/components/ListingFilter/ListingFilter";
    import {ListingsStore} from "$lib/stores/ListingsStore.svelte";

    let {attributes}: ListingFilterProps = $props()

    let searchType = $state("normal")

    function invert() {
        ListingsStore.value.searchParams.searchAttributes = attributes
            ?.map(a => a.normalized)
            .filter(a => ListingsStore.value.searchParams.searchAttributes.find(s => s === a) === undefined)
    }

    let attrs = $derived(attributes?.toSorted((a, b) => a.label.localeCompare(b.label)))


</script>

<div class=col>
    <div role="group">
        <details class="dropdown">
            <summary></summary>
            <ul>
                <li onclick={invert}>Search in...</li>
                {#each attrs as attribute}
                    <li>
                        <label>
                            <input type="checkbox"
                                   name="attributes"
                                   value={attribute.normalized}
                                   bind:group={ListingsStore.value.searchParams.searchAttributes}
                                   checked={ListingsStore.value.searchParams.searchAttributes.indexOf(attribute?.normalized) > -1}
                            />
                            {attribute.label}
                        </label>
                    </li>
                {/each}
            </ul>
        </details>
        <details class="dropdown">
            <summary></summary>
            <ul>
                <li>Predefined searches...</li>
                <li>
                    <label>
                        <input bind:group={searchType}
                               checked={searchType === "normal"}
                               name="searchType"
                               type="radio"
                               value="normal"/>
                        Normal Search
                    </label>
                </li>
                <li>
                    <label>
                        <input bind:group={searchType}
                               checked={searchType === "userListing"}
                               name="searchType"
                               type="radio"
                               value="userListing"/>
                        User Edited
                    </label>
                </li>
            </ul>
        </details>
        <input bind:value={ListingsStore.value.searchParams.searchString} disabled={searchType !== "normal"} type="search" />
        <button onclick={() => {ListingsStore.value.searchParams.searchString = "" }}>X</button>
    </div>
</div>
