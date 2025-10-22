<script lang="ts">
    import type {InputProps} from "$lib/components/Input/Input";

    let {
        value,
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
                onsubmit(ev.target.value)
            } else {
                onchange(oldValue, ev.target.value)
                oldValue = ev.target.value
            }
            }}
       {placeholder}
       type="text"
       {value}
/>
