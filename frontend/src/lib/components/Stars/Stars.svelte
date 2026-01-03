<script lang="ts">
    import Star from '$images/star.svg?component'
    let { value, maxValue, onchange  = () => {} } : {value: number, maxValue: number, onchange: (newValue: number) => void} = $props()

    let hover: number | undefined = $state()

    function hovering(nr: number) {
        hover = nr
    }

</script>
<i-stars-wrapper on:mouseout={() => hover = undefined}>
{#each { length: maxValue } as _,  nr }
    <i-star-wrapper on:mouseover={() => hovering(nr)} on:click={() => onchange(nr + 1)}>
    <Star
            fill={(nr < value) ? "#ff0" : "#999"}
            stroke={(hover && nr <= hover) ? "#f00" : "none"}
            stroge-width={(hover &&  nr <= hover) ? "5" : "none"}
    />
    </i-star-wrapper>
{/each}
</i-stars-wrapper>