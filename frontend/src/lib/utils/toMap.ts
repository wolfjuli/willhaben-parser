/**
 *
 */
export function toMap<T extends object, V>(
    // @ts-ignore
    keySelector: (obj: T) => string | number = obj => obj.id,
    valueSelector: (obj: T) => V = obj => obj as unknown as V
): (acc: { [key: string | number]: V }, curr: T) => ({ [key: string]: V }) {
    return (acc, curr) => {
        let a = typeof acc === "object" ? acc : {}
        a[keySelector(curr)] = valueSelector(curr)
        return a
    }
}
