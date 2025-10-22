import { z as once, A as run } from "./index2.js";
class WithState {
  value;
}
class WithLocalStore extends WithState {
  key = "";
  constructor(key, initialValue) {
    super();
    this.key = key;
    {
      this.value = initialValue();
    }
  }
  serialize(value) {
    return JSON.stringify(value);
  }
  deserialize(item) {
    return JSON.parse(item);
  }
}
class FetchingStore {
  static #instance;
  fetchingStores = {};
  #value = once(() => Object.values(FetchingStore.instance.fetchingStores).reduce((a, c) => a + c, 0));
  get value() {
    return this.#value();
  }
  static get fetching() {
    return FetchingStore.instance.value;
  }
  static get instance() {
    if (!FetchingStore.#instance) FetchingStore.#instance = new FetchingStore();
    return FetchingStore.#instance;
  }
  static whileFetching(store, block) {
    FetchingStore.startFetching(store);
    let ret;
    try {
      ret = block();
    } finally {
      if (ret) ret.finally(() => FetchingStore.finishFetching(store));
      else FetchingStore.finishFetching(store);
    }
    return ret;
  }
  static startFetching(store) {
    FetchingStore.instance.fetchingStores[store] = (FetchingStore.instance.fetchingStores[store] ?? 0) + 1;
  }
  static finishFetching(store) {
    if (FetchingStore.instance.fetchingStores[store] && FetchingStore.instance.fetchingStores[store] === 1) delete FetchingStore.instance.fetchingStores[store];
  }
}
function isListing(l) {
  if (!l) return false;
  const t = l;
  return t.id !== void 0 && t.willhabenId !== void 0;
}
function isKnownListing(l) {
  if (!l || !l.listing || !l.md5) return false;
  const t = l.listing;
  return t.id !== void 0 && t.willhabenId !== void 0;
}
class ListingDb {
  static #instance;
  db;
  asyncInstance;
  constructor() {
    const req = indexedDB.open("willhaben", 3);
    this.asyncInstance = new Promise((resolve) => {
      req.onupgradeneeded = () => {
        this.db = req.result;
        const objectStore = this.db.createObjectStore("listings", { keyPath: "id" });
        objectStore.createIndex("willhabenId", "willhabenId", { unique: false });
        objectStore.createIndex("heading", "heading", { unique: false });
        const knownMd5 = this.db.createObjectStore("knownMd5", { keyPath: "id" });
        knownMd5.createIndex("md5", "md5", { unique: true });
        resolve(this);
      };
      req.onsuccess = () => {
        this.db = req.result;
        resolve(this);
      };
    });
  }
  static get instance() {
    if (!ListingDb.#instance) ListingDb.#instance = new ListingDb();
    return ListingDb.#instance.asyncInstance;
  }
  static async addAll(elements) {
    return this.instance.then((inst) => {
      const listingsStore = inst.db.transaction("listings", "readwrite").objectStore("listings");
      const knownStore = inst.db.transaction("knownMd5", "readwrite").objectStore("knownMd5");
      elements.forEach((e) => {
        if (isListing(e)) {
          listingsStore.delete(e.id);
          listingsStore.add(e);
        } else if (isKnownListing(e)) {
          listingsStore.delete(e.listing.id);
          listingsStore.add(e.listing);
          knownStore.delete(e.listing.id);
          knownStore.add({ id: e.listing.id, md5: e.md5 });
        } else {
          knownStore.delete(e.id);
          knownStore.add(e);
        }
      });
    });
  }
  static async get(id) {
    return new Promise(async (resolve, reject) => {
      this.instance.then((inst) => {
        const req = inst.db.transaction("listings", "readonly").objectStore("listings").get(id);
        req.onsuccess = () => resolve(req.result);
        req.onerror = () => reject();
      });
    });
  }
  static async known(ids) {
    return new Promise(async (resolve, reject) => {
      let md5s = [];
      let fails = 0;
      this.instance.then((inst) => {
        ids.map((id) => {
          const req = inst.db.transaction("knownMd5", "readonly").objectStore("knownMd5").get(id);
          req.onsuccess = () => {
            const md5 = req.result?.md5;
            if (md5) {
              md5s = [...md5s, req.result.md5];
            } else {
              fails++;
            }
            if (md5s.length + fails === ids.length) resolve(md5s);
          };
          req.onerror = () => fails++;
        });
      });
    });
  }
}
class ListingsStore extends WithLocalStore {
  static #instance;
  constructor() {
    super("listingsStore", () => ({
      sorting: [],
      lastUpdate: /* @__PURE__ */ new Date("2020-01-01"),
      searchParams: {
        viewAttributes: [],
        searchString: "",
        searchAttributes: [],
        sortCol: "points",
        sortDir: "DESC"
      }
    }));
    if (/* @__PURE__ */ (/* @__PURE__ */ new Date()).valueOf() - new Date(this.value.lastUpdate).valueOf() > 1e4) this.fetchSorting();
  }
  static get instance() {
    if (!this.#instance) this.#instance = new ListingsStore();
    return this.#instance;
  }
  static get value() {
    return ListingsStore.instance.value;
  }
  fetchSorting() {
    FetchingStore.whileFetching("fetchSorting", () => {
      const params = this.value.searchParams;
      const attrs = [
        .../* @__PURE__ */ new Set([
          ...params.searchAttributes,
          ...params.viewAttributes
        ])
      ].join(",");
      fetch(`/api/rest/v1/listings/sorting?sortCol=${params.sortCol}&sortDir=${params.sortDir}&searchString=${params.searchString}&searchAttrs=${attrs}`).then((r) => r.json()).then((sorting) => {
        run(() => this.value.lastUpdate = /* @__PURE__ */ new Date());
        run(() => this.value.sorting = sorting.map((s) => s.listingId));
      });
    });
  }
  fetch(listingIds) {
    return FetchingStore.whileFetching("fetchListing", () => {
      return run(() => ListingDb.known(listingIds).then(async (knownMd5) => await fetch(`/api/rest/v1/listings/full?ids=${listingIds.join(",")}&knownMd5=${knownMd5.join(",")}`)).then((r) => r.json()).then(async (full) => {
        this.value.lastUpdate = /* @__PURE__ */ new Date();
        await ListingDb.addAll(full.map((f) => ({ listing: f.listing, md5: f.md5 })));
      }));
    });
  }
  static createListingValue = (listingValue) => FetchingStore.whileFetching("createListingValue", () => fetch("/api/rest/v1/user_defined_attributes", {
    method: "post",
    body: JSON.stringify(listingValue)
  }).then(() => ListingsStore.instance.fetch([listingValue.listingId])));
  static updateListingValue = (listingValue) => FetchingStore.whileFetching("updateListingValue", () => fetch("/api/rest/v1/user_defined_attributes", {
    method: "put",
    body: JSON.stringify(listingValue)
  }).then(() => ListingsStore.instance.fetch([listingValue.listingId])));
  static deleteListingValue = (listingValue) => FetchingStore.whileFetching("deleteListingValue", () => fetch("/api/rest/v1/user_defined_attributes", {
    method: "delete",
    body: JSON.stringify(listingValue)
  }).then(() => ListingsStore.instance.fetch([listingValue.listingId])));
}
export {
  FetchingStore as F,
  ListingsStore as L,
  WithState as W,
  ListingDb as a
};
