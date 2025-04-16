export abstract class WithState<T> {
    value = $state<T>() as T
}
