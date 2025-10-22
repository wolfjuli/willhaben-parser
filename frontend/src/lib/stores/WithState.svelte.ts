export abstract class WithState<T> {
    value = $state<T>() as T

    constructor(value: T) {
        this.value = value
    }
}
