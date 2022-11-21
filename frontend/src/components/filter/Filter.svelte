<script lang="ts">
    import translations from "../../data/translations";
    import {prevent_default} from "svelte/internal";

    enum EAction {
        Greater,
        GreaterEquals,
        Equals,
        SmallerEquals,
        Smaller,
        NotEquals,
        Contains,
        NotContains
    }

    const translationKeys = [
        "EAction.Greater",
        "EAction.GreaterEquals",
        "EAction.Equals",
        "EAction.SmallerEquals",
        "EAction.Smaller",
        "EAction.NotEquals",
        "EAction.Contains",
        "EAction.NotContains"
    ]

    let currentAction: EAction = EAction.Equals

    function clearFilters() {

    }

</script>

<nav class="navbar navbar-expand-lg navbar-light bg-light">
    <div class="container-fluid">
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarSupportedContent"
                aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item">
                    <a class="nav-link active" aria-current="page" href="#">Clear all Filters</a>
                </li>
            </ul>
            <form class="d-flex">
                <input class="form-control me-2" type="text" placeholder="Field name" aria-label="fieldName">
                <div class="nav-item dropdown btn">
                    <a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button"
                       data-bs-toggle="dropdown" aria-expanded="false">
                        {translations["en"][translationKeys[currentAction]]}
                    </a>

                    <ul class="dropdown-menu" aria-labelledby="navbarDropdown">
                        {#each Object.keys(EAction) as action}
                            {#if !isNaN(action)}
                                <li><a
                                        on:click={prevent_default(() => currentAction = action)}
                                        class="dropdown-item {currentAction === action ? 'active' : ''}"
                                        href="#">
                                    {translations["en"][translationKeys[action]]}
                                </a>
                                </li>
                            {/if}
                        {/each}
                    </ul>
                </div>
                <input class="form-control me-2" type="search" placeholder="Value" aria-label="value">
                <button class="btn btn-outline-success" type="submit">Add</button>
            </form>
        </div>
    </div>
</nav>