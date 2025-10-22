import type {Snippet} from "svelte";

export interface TableProps<T> {
    tableData: T[]
    thead: Snippet
    row: Snippet<[T, number]>

}
