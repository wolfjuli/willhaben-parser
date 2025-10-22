export type IDObject = { id: string | number, [key: string]: unknown }

export interface DropdownProps<T extends IDObject> {
    onchange: (attribute: T) => boolean,
    values: T[]
    nameSelector: (v: T) => string
    emptyFirstLineText: string | undefined
    preSelected: string | number | undefined
}
