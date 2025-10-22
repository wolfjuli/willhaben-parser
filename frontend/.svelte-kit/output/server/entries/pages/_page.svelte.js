import { h as pop, p as push } from "../../chunks/index2.js";
import { L as ListingsStore } from "../../chunks/ListingsStore.svelte.js";
import { L as ListingTable, s as settingsStore } from "../../chunks/settings.svelte.js";
import { f as filteredAttributes, m as mergedAttributes } from "../../chunks/attributes.svelte.js";
function _page($$payload, $$props) {
  push();
  let { data } = $$props;
  let configuration = data.configuration;
  const settings = settingsStore.value;
  const sorting = ListingsStore.value.sorting;
  const fields = filteredAttributes(settings.listingFields);
  const attributes = mergedAttributes().value;
  ListingsStore.value.searchParams;
  ListingTable($$payload, { attributes, sorting, configuration, fields });
  pop();
}
export {
  _page as default
};
