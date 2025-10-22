type Entries<T> = {
    [K in keyof T]: [K, T[K]]
}[keyof T][]

type Keys<T> = (keyof T)[]

export function objectEntries<T extends object>(object: T): Entries<T> {
    return Object.entries(object) as Entries<T>
}

export function objectKeys<T extends object>(object: T): Keys<T> {
    return Object.keys(object) as Keys<T>
}

export function nestedValue(
    object: Record<string, unknown>,
    path: string,
): unknown {
    const parts = path.split('.')
    let current: unknown = object
    for (const key of parts) {
        if (current === null || current === undefined) return current
        if (typeof current !== 'object') return undefined
        current = (current as Record<string, unknown>)[key]
    }
    return current
}

export const hasOwn = <T extends object, K extends PropertyKey>(
    obj: T,
    key: K,
): obj is T & Record<K, unknown> => Object.hasOwn(obj, key)

export const fromEntries = <K extends string | number | symbol, V>(
    array: [K, V][],
): Record<K, V> => Object.fromEntries(array) as Record<K, V>

export const using = <T, R>(value: T, block: (value: T) => R): R => block(value)
