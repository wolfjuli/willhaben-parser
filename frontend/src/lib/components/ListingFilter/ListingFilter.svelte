<script lang="ts">

    import type {ListingFilterProps} from "$lib/components/ListingFilter/ListingFilter";
    import {SearchParamsStore} from "$lib/stores/SearchParamsStore.svelte";

    let {attributes}: ListingFilterProps = $props()

    let searchType = $state("normal")

    function invert() {
        SearchParamsStore.value.searchAttributes = attributes
            ?.map(a => a.attribute)
            .filter(a => SearchParamsStore.value.searchAttributes.find(s => s === a) === undefined)
    }

    let attrs = $derived(attributes?.toSorted((a, b) => (a.label ?? a.attribute)?.localeCompare((b.label ?? b.attribute))))

    let oldSearchString = SearchParamsStore.value.searchString
    let searchString = $state(SearchParamsStore.value.searchString)
    let timer = 0;

    $effect(() => {
        if(searchString != oldSearchString) {
            if(timer) clearTimeout(timer);
            oldSearchString = searchString
            console.log("Resetting timer")
            timer = setTimeout(() => {
                clearTimeout(timer);
                timer = 0
                console.log("Setting search")
                if(!SearchParamsStore.value.searchAttributes.length) SearchParamsStore.value.searchAttributes = [...SearchParamsStore.value.viewAttributes];
                SearchParamsStore.value.searchString = searchString
                SearchParamsStore.set(SearchParamsStore.value)
            }, 500)
        }
    })

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
                                   value={attribute.attribute}
                                   bind:group={SearchParamsStore.value.searchAttributes}
                                   checked={SearchParamsStore.value.searchAttributes.indexOf(attribute?.attribute) > -1}
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
        <input bind:value={searchString} disabled={searchType !== "normal"} type="search"/>
        <button onclick={() => {SearchParamsStore.value.searchString = "" }}>X</button>
    </div>
</div>
