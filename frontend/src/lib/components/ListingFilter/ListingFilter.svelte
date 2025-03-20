<script lang="ts">

    import type {ListingFilterProps} from "$lib/components/ListingFilter/ListingFilter";

    let {userListings, listings, attributes, onchange}: ListingFilterProps = $props()

    let searchTerm = $state("")
    let searchType = $state("normal")
    let selectedAttributes = $state<string[]>(attributes.map(a => a.normalized))

    $effect(() => onchange((listing): boolean => {
        if (searchType === "normal") {
            const parts = searchTerm.split(" ").filter(Boolean)
            return parts.every(word =>
                selectedAttributes.find(attr =>
                    listing?.[attr]?.toString().toLowerCase().includes(word) === true
                ) !== undefined) === true
        } else {
            return Object.keys(userListings).find(k => +k === listing?.willhabenId) !== undefined
        }
    }))

    function invert() {
        selectedAttributes = attributes
            .map(a => a.normalized)
            .filter(a => selectedAttributes.find(s => s === a) === undefined)
    }
</script>

<div class=col>
    <div role="group">
        <details class="dropdown">
            <summary></summary>
            <ul>
                <li onclick={invert}>Search in...</li>
                {#each attributes as attribute}
                    <li>
                        <label>
                            <input type="checkbox"
                                   name="attributes"
                                   value={attribute.normalized}
                                   bind:group={selectedAttributes}
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
        <input bind:value={searchTerm} disabled={searchType !== "normal"} type="search"/>
        <button onclick={() => {searchTerm = "" }}>X</button>
    </div>
</div>
