import { o as ensure_array_like, n as escape_html, m as attr, h as pop, p as push } from "../../../chunks/index2.js";
import { L as ListingsStore } from "../../../chunks/ListingsStore.svelte.js";
import { L as ListingTable, s as settingsStore, a as setSettings } from "../../../chunks/settings.svelte.js";
import { f as filteredAttributes, m as mergedAttributes } from "../../../chunks/attributes.svelte.js";
import "bootstrap";
/* empty css                       */
import { D as Dropdown } from "../../../chunks/Dropdown.js";
import "../../../chunks/functions.svelte.js";
function _page($$payload, $$props) {
  push();
  let { data } = $$props;
  ListingsStore.value.listings ?? {};
  const sorting = ListingsStore.value.sorting ?? [];
  const settings = settingsStore.value;
  const fields = filteredAttributes(settings.listingFields);
  const attributes = mergedAttributes().value?.filter((a) => !fields.find((f) => f.normalized === a.normalized)) ?? [];
  function add(attr2) {
    const listingFields = [...settings.listingFields, attr2.normalized];
    setSettings({ ...settings, listingFields });
    return true;
  }
  let maxIdx = fields.length - 1;
  const each_array = ensure_array_like(fields);
  $$payload.out += `<details class="svelte-1cz0t9r"><summary class="svelte-1cz0t9r">Columns</summary> <div class="container-fluid svelte-1cz0t9r"><!--[-->`;
  for (let idx = 0, $$length = each_array.length; idx < $$length; idx++) {
    let field = each_array[idx];
    $$payload.out += `<div class="row svelte-1cz0t9r"><div class="col-1">${escape_html(field.label ?? field.normalized)}</div> <div class="col-4"><button${attr("disabled", idx === 0, true)}>&lt;</button> <button>X</button> <button${attr("disabled", idx === maxIdx, true)}>></button></div></div>`;
  }
  $$payload.out += `<!--]--> <div class="row svelte-1cz0t9r">`;
  Dropdown($$payload, {
    emptyFirstLineText: "Add attribute...",
    nameSelector: (v) => v.label ?? v.normalized,
    onchange: (attr2) => add(attr2),
    values: attributes
  });
  $$payload.out += `<!----></div></div> `;
  ListingTable($$payload, {
    configuration: data.configuration,
    attributes,
    fields,
    sorting
  });
  $$payload.out += `<!----></details>`;
  pop();
}
export {
  _page as default
};
