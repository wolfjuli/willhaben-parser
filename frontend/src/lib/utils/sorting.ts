export function sortNumber(a: number, b: number, ascending: boolean) {
    return ascending ? a - b : b - a
}

export function sortString(a: string | undefined, b: string | undefined, ascending: boolean) {
    console.log(a, b)
    return ascending ? a?.localeCompare(b ?? "") : b?.localeCompare(a ?? "")
}

