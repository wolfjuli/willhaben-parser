import {
    type Listing, type RawSorting,
} from '../types/Listing';
import {isKnownListing, isListing} from "$lib/utils/checkType";


export class ListingDb {
    static #instance: ListingDb

    private db: IDBDatabase

    private asyncInstance: Promise<ListingDb>

    private constructor() {
        const req = indexedDB
            .open("willhaben", 3)

        this.asyncInstance = new Promise((resolve) => {
            req
                .onupgradeneeded = () => {
                this.db = req.result
                const objectStore = this.db.createObjectStore("listings", {keyPath: "id"});

                objectStore.createIndex("willhabenId", "willhabenId", {unique: false});
                objectStore.createIndex("heading", "heading", {unique: false});

                const knownMd5 = this.db.createObjectStore("knownMd5", {keyPath: "id"});
                knownMd5.createIndex("md5", "md5", {unique: true})

                resolve(this)
            }
            req.onsuccess = () => {
                this.db = req.result
                resolve(this)
            }
        })
    }

    private static get instance(): Promise<ListingDb> {
        if (!ListingDb.#instance)
            ListingDb.#instance = new ListingDb()

        return ListingDb.#instance.asyncInstance
    }

    static async addAll(elements: Listing[] | { id: number, md5: string }[] | {
        listing: Listing,
        md5: string
    }[]): Promise<void> {
        return this.instance.then(inst => {
            const listingsStore = inst.db
                .transaction("listings", "readwrite")
                .objectStore("listings")
            const knownStore = inst.db
                .transaction("knownMd5", "readwrite")
                .objectStore("knownMd5")

            elements.forEach(e => {
                if (isListing(e)) {
                    listingsStore.delete(e.id)
                    listingsStore.add(e)
                } else if (isKnownListing(e)) {
                    listingsStore.delete(e.listing.id)
                    listingsStore.add(e.listing)
                    knownStore.delete(e.listing.id)
                    knownStore.add({id: e.listing.id, md5: e.md5})
                } else {
                    knownStore.delete(e.id)
                    knownStore.add(e)
                }
            })
        })
    }

    static async get(id: number): Promise<Listing> {
        return new Promise(async (resolve, reject) => {
                this.instance.then(inst => {
                    const req = inst.db.transaction("listings", "readonly").objectStore("listings").get(id)
                    req.onsuccess = () => resolve(req.result)
                    req.onerror = () => reject()
                })
            }
        )
    }

    static async known(ids: number[]): Promise<string[]> {
        return new Promise(async (resolve, reject) => {
                let md5s: string[] = []
                let fails: number = 0
                this.instance.then(inst => {
                    ids.map(id => {
                        const req = inst.db.transaction("knownMd5", "readonly").objectStore("knownMd5").get(id)
                        req.onsuccess = () => {
                            const md5 = req.result?.md5
                            if (md5) {
                                md5s = [...md5s, req.result.md5]
                            } else {
                                fails++
                            }

                            if (md5s.length + fails === ids.length)
                                resolve(md5s)
                        }
                        req.onerror = () => fails++
                    })
                })
            }
        )
    }
}
