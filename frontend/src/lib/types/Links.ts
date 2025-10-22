export type Link = {
    href: string,
    value: string
}

export function isLink(obj: { [key: string]: unknown } | string | number): obj is Link {
    return typeof obj === "object" &&
        obj.href !== undefined && typeof obj.href === "string" &&
        obj.value !== undefined && typeof obj.value === "string"
}
