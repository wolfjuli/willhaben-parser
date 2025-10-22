import { o as ensure_array_like, m as attr, n as escape_html, h as pop, p as push } from "./index2.js";
import { L as ListingsStore } from "./ListingsStore.svelte.js";
function ListingFilter($$payload, $$props) {
  push();
  let { attributes } = $$props;
  let searchType = "normal";
  let attrs = attributes?.toSorted((a, b) => a.label.localeCompare(b.label));
  const each_array = ensure_array_like(attrs);
  $$payload.out += `<div class="col"><div role="group"><details class="dropdown"><summary></summary> <ul><li>Search in...</li> <!--[-->`;
  for (let $$index = 0, $$length = each_array.length; $$index < $$length; $$index++) {
    let attribute = each_array[$$index];
    $$payload.out += `<li><label><input type="checkbox" name="attributes"${attr("value", attribute.normalized)}${attr("checked", ListingsStore.value.searchParams.searchAttributes.includes(attribute.normalized), true)}${attr("checked", ListingsStore.value.searchParams.searchAttributes.indexOf(attribute?.normalized) > -1, true)}> ${escape_html(attribute.label)}</label></li>`;
  }
  $$payload.out += `<!--]--></ul></details> <details class="dropdown"><summary></summary> <ul><li>Predefined searches...</li> <li><label><input${attr("checked", searchType === "normal", true)}${attr("checked", searchType === "normal", true)} name="searchType" type="radio" value="normal"> Normal Search</label></li> <li><label><input${attr("checked", searchType === "userListing", true)}${attr("checked", searchType === "userListing", true)} name="searchType" type="radio" value="userListing"> User Edited</label></li></ul></details> <input${attr("value", ListingsStore.value.searchParams.searchString)}${attr("disabled", searchType !== "normal", true)} type="search"> <button>X</button></div></div>`;
  pop();
}
export {
  ListingFilter as L
};
