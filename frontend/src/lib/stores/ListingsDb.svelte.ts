import {
    type Listing,
} from '../types/Listing';


export class ListingDb {
    static #instance: ListingDb

    private db: IDBDatabase

    private asyncInstance: Promise<ListingDb>

    private constructor() {
        const req = indexedDB
            .open("willhaben")

        this.asyncInstance = new Promise((resolve) => {
            req
                .onupgradeneeded = (ev: IDBVersionChangeEvent) => {
                this.db = ev.target!!.result;
                const objectStore = this.db.createObjectStore("listings", {keyPath: "id"});

                objectStore.createIndex("willhabenId", "willhabenId", {unique: false});
                objectStore.createIndex("heading", "heading", {unique: false});
                resolve(this)
            }
            req.onsuccess = ev => {
                this.db = ev.target!!.result;
                resolve(this)
            }
        })
    }

    private static get instance(): Promise<ListingDb> {
        if (!ListingDb.#instance)
            ListingDb.#instance = new ListingDb()

        return ListingDb.#instance.asyncInstance
    }

    static async addAll(listing: Listing[]): Promise<void> {
        return this.instance.then(inst => {
            const listingsStore = inst.db
                .transaction("listings", "readwrite")
                .objectStore("listings")


            listing.forEach(l => {
                listingsStore.delete(l.id)
                listingsStore.add(l)
            })
        })
    }

    static async get(id: number): Promise<Listing> {
        return new Promise(async (resolve, reject) => {
                this.instance.then(inst => {
                    const req = inst.db.transaction("listings", "readonly").objectStore("listings").get(id)
                    req.onsuccess = () => resolve(req.result)
                    req.onerror = (ev) => reject()
                })
            }
        )
    }
}
