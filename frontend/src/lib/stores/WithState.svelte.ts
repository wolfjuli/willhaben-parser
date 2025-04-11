export abstract class WithState<T> {
    protected value = $state<T>() as T;
}
