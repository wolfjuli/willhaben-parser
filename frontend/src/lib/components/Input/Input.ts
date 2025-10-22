export type InputProps = {
    value: string,
    placeholder: string | undefined,
    onsubmit: (val: string) => void
    onchange: (oldVal: string, val: string) => void
}
