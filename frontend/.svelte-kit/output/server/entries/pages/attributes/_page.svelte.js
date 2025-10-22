import { o as ensure_array_like, n as escape_html, m as attr, h as pop, p as push } from "../../../chunks/index2.js";
import { L as ListingFilter } from "../../../chunks/ListingFilter.js";
import { F as FunctionsStore } from "../../../chunks/functions.svelte.js";
import { S as ScriptsStore } from "../../../chunks/ScriptsStore.svelte.js";
import { m as mergedAttributes, C as CustomAttributesStore } from "../../../chunks/attributes.svelte.js";
import { F as Function } from "../../../chunks/Function.js";
function _page($$payload, $$props) {
  push();
  const functions = FunctionsStore.value;
  ScriptsStore.value;
  const attributes = mergedAttributes().value?.toSorted((a, b) => a.normalized.localeCompare(b.normalized)) ?? [];
  const customAttributes = CustomAttributesStore.value ?? [];
  const { data } = $$props;
  let newAttr = {
    normalized: "",
    label: "",
    dataType: ""
  };
  const each_array = ensure_array_like(customAttributes);
  ListingFilter($$payload, { attributes });
  $$payload.out += `<!----> <!--[-->`;
  for (let $$index = 0, $$length = each_array.length; $$index < $$length; $$index++) {
    let attribute = each_array[$$index];
    $$payload.out += `<e:attribute><h3>${escape_html(attribute.label)} <small>[${escape_html(attribute.dataType)}]</small></h3> <div>`;
    Function($$payload, { fun: functions[attribute.functionId] });
    $$payload.out += `<!----></div> <hr></e:attribute>`;
  }
  $$payload.out += `<!--]--> <div role="group"><input${attr("value", newAttr.normalized)} placeholder="Attribute Name" type="text"> <input${attr("value", newAttr.label)} placeholder="Label" type="text"> <input${attr("value", newAttr.dataType)} placeholder="Data Type" type="text"> <button>+</button></div>`;
  pop();
}
export {
  _page as default
};
