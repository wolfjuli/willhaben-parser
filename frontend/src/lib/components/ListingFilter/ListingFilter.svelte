<script lang="ts">

    import type {ListingFilterProps} from "$lib/components/ListingFilter/ListingFilter";
    import {ListingSearchParams} from "$lib/stores/ListingsStore.svelte.js";

    let {attributes}: ListingFilterProps = $props()

    let searchType = $state("normal")

    function invert() {
        ListingSearchParams.searchAttributes = attributes
            ?.map(a => a.normalized)
            .filter(a => ListingSearchParams.searchAttributes.find(s => s === a) === undefined)
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
                                   bind:group={ListingSearchParams.searchAttributes}
                                   checked={ListingSearchParams.searchAttributes.indexOf(attribute?.normalized) > -1}
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
        <input bind:value={ListingSearchParams.searchString} disabled={searchType !== "normal"} type="search" />
        <button onclick={() => {ListingSearchParams.searchString = "" }}>X</button>
    </div>
</div>
