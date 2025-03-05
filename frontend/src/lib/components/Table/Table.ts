import type {Snippet} from "svelte";

interface Data {
    [key: string]: unknown
}


export interface TableProps {
    tableData: Data[]
    thead: Snippet
    row: Snippet<[unknown, number]>

}
