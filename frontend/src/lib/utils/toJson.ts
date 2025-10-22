export function toJson<T>(text: string | null | undefined): T | undefined {
    return text ? JSON.parse(text) : undefined
}
