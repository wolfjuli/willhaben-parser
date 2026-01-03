export type InputProps = {
    value: string,
    placeholder: string | undefined,
    onsubmit: (val: string, target: HTMLInputElement) => void
    onchange: (oldVal: string, val: string, target: HTMLInputElement) => void
}
