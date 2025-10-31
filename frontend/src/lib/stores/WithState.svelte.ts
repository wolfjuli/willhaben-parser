export abstract class WithState<T> {
    value = $state<T>() as T

    constructor(value: T) {
        this.value = value
    }

    protected static upsert(value: any ): any {
        return value
    }

    static set(value: any): any {  throw new Error("Set is not allowed here!") }
    static delete<I>(value: any): any  {  throw new Error("Delete is not allowed here!") }
}
