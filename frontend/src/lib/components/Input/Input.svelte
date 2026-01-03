<script lang="ts">
    import type {InputProps} from "$lib/components/Input/Input";

    let {
        value = $bindable(),
        placeholder,
        onsubmit = () => {
        },
        onchange = () => {
        }
    }: InputProps = $props()
    let oldValue = value


</script>

<input onkeyup={(ev: KeyboardEvent) => {
            if(ev.key === "Enter"){
                onsubmit(ev.target.value, ev.target)
            } else {
                onchange(oldValue, ev.target.value, ev.target)
                oldValue = ev.target.value
            }
            }}
       {placeholder}
       type="text"
       bind:value={value}
/>
