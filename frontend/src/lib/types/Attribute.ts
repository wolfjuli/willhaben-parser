export type Attribute = BaseAttribute | CustomAttribute

export type BaseAttribute = {
    id: number,
    attribute: string,
    label: string,
    sortingAttribute: string | undefined,
    dataType: string
}

export type CustomAttribute = {
    id: number,
    attribute: string
    label: string,
    dataType: string,
    functionId: number,
    sortingAttribute: string | undefined,
    replaces: boolean
}

export type CreateCustomAttribute = {
    id: number,
    normalized: string
    label: string,
    dataType: string,
    functionId: number
}

export type CreateUserAttribute = {
    attributeId: number,
    listingId: number,
    values: string
}
