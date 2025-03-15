export type Attribute = BaseAttribute | CustomAttribute

export type BaseAttribute = {
    id: number,
    attribute: string,
    normalized: string
    label: string,
    dataType: string
}

export type CustomAttribute = {
    id: number,
    normalized: string
    label: string,
    dataType: string,
    functionId: number,
    replaces: boolean
}
