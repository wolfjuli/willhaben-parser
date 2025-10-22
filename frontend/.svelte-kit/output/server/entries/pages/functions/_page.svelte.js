import { n as escape_html, h as pop, p as push, m as attr, o as ensure_array_like, q as spread_props, t as await_block } from "../../../chunks/index2.js";
import { F as FunctionsStore } from "../../../chunks/functions.svelte.js";
import { S as ScriptsStore } from "../../../chunks/ScriptsStore.svelte.js";
import { r as randomName, F as Function } from "../../../chunks/Function.js";
import { D as Dropdown } from "../../../chunks/Dropdown.js";
import { L as ListingsStore, a as ListingDb } from "../../../chunks/ListingsStore.svelte.js";
/* empty css                                                          */
import { m as mergedAttributes } from "../../../chunks/attributes.svelte.js";
function CustomScript($$payload2, $$props2) {
  push();
  let { script: script2, attributes: attributes2 } = $$props2;
  function save() {
    if (!script2.name) {
      ScriptsStore.delete(script2);
    } else {
      ScriptsStore.update(script2);
    }
  }
  $$payload2.out += `<div class="grid"><div>`;
  {
    $$payload2.out += "<!--[!-->";
    $$payload2.out += `<span>${escape_html(script2.name)}</span>`;
  }
  $$payload2.out += `<!--]--></div> <div>`;
  Dropdown($$payload2, {
    nameSelector: (a) => a.label,
    onchange: (a) => {
      script2.attributeId = a.id;
      save();
      return false;
    },
    preSelected: script2.attributeId,
    values: attributes2
  });
  $$payload2.out += `<!----></div></div>`;
  pop();
}
function CreateCustomScript($$payload2, $$props2) {
  push();
  let { attributes: attributes2 } = $$props2;
  function newScript() {
    return {
      id: -1,
      name: randomName(),
      attributeId: -1
    };
  }
  function save() {
    ScriptsStore.create(script2);
    script2 = newScript();
  }
  function selected(a) {
    script2.attributeId = a.id;
    save();
    return true;
  }
  let script2 = newScript();
  $$payload2.out += `<div${attr("id", `fun${script2.id ?? "-new"}`)} class="grid"><div>New Script on attribute:</div> <div>`;
  Dropdown($$payload2, {
    nameSelector: (a) => a.label,
    onchange: selected,
    values: attributes2
  });
  $$payload2.out += `<!----></div></div>`;
  pop();
}
function FunctionValue($$payload2, $$props2) {
  let { value: value2, name } = $$props2;
  $$payload2.out += `<div><span class="svelte-bf09ua">${escape_html(name)}</span> <small>${escape_html(value2 ?? "")}</small></div>`;
}
function transformListing(listing, attributes, functions) {
  return attributes.reduce((lst, attr) => {
    const fun = attr.functionId ? eval(functions[attr.functionId].function) : () => lst[attr.normalized];
    lst[attr.normalized] = fun(lst[attr.normalized], lst);
    return lst;
  }, { ...listing });
}
function ScriptFunctions($$payload, $$props) {
  push();
  let { script, listing, attributes, functions } = $$props;
  let attribute = attributes.find((a) => a.id === script.attributeId);
  let attributeValue = (listing ? transformListing(listing, attributes, functions) : void 0)?.[attribute.normalized]?.base;
  let functionValues = script.functions?.reduce(
    (acc, fId) => {
      const fun = functions[fId.functionId];
      if (!fun) return acc;
      let exec = void 0;
      let value = void 0;
      try {
        exec = fun?.function ? eval(fun.function) : void 0;
        value = listing && exec ? exec(acc[acc.length - 1].value, listing) : [];
      } catch (e) {
        console.error(`Error during execution of function '${fun.name}' on '${attribute.normalized}'`, e);
      }
      return [
        ...acc,
        {
          scriptId: script.id,
          functionId: fun.id,
          ord: fId.ord,
          name: fun.name,
          value
        }
      ];
    },
    [
      {
        name: attribute?.normalized,
        value: attributeValue,
        ord: -1,
        functionId: -1,
        scriptId: -1
      }
    ]
  ) ?? [];
  function addFunction(fun2) {
    let ord = script.functions[script.functions.length - 1].ord + 1;
    let newScript = { scriptId: script.id, functionId: fun2.id, ord };
    ScriptsStore.createScriptFunction(newScript);
  }
  const each_array = ensure_array_like(functionValues.slice(1));
  $$payload.out += `<details><summary>Function steps `;
  if (listing && functionValues) {
    $$payload.out += "<!--[-->";
    $$payload.out += `(${escape_html(functionValues[functionValues.length - 1].value)} Points)`;
  } else {
    $$payload.out += "<!--[!-->";
  }
  $$payload.out += `<!--]--></summary> <div class="text-center">`;
  FunctionValue($$payload, spread_props([functionValues[0]]));
  $$payload.out += `<!----> <!--[-->`;
  for (let $$index = 0, $$length = each_array.length; $$index < $$length; $$index++) {
    let fv = each_array[$$index];
    $$payload.out += `<!---->↓ <br> <button>X</button> `;
    FunctionValue($$payload, spread_props([fv]));
    $$payload.out += `<!----> <br>`;
  }
  $$payload.out += `<!--]--> `;
  Dropdown($$payload, {
    nameSelector: (e) => e.name,
    onchange: addFunction,
    values: Object.values(functions)
  });
  $$payload.out += `<!----></div></details>`;
  pop();
}
function ListingSearch($$payload2, $$props2) {
  push();
  let { sorted } = $$props2;
  const each_array2 = ensure_array_like(sorted);
  $$payload2.out += `<div role="group"><input type="search"${attr("value", ListingsStore.value.searchParams.searchString)}> <button>X</button></div> <select size="5"><!--[-->`;
  for (let $$index = 0, $$length = each_array2.length; $$index < $$length; $$index++) {
    let id = each_array2[$$index];
    $$payload2.out += `<!---->`;
    await_block(
      ListingDb.get(id),
      () => {
      },
      (listing2) => {
        $$payload2.out += `<option${attr("value", id)}>${escape_html(`${listing2.willhabenId} - ${listing2.heading.base.slice(0, 30)} - ${listing2.points} - ${listing2.priceForDisplay.base}`)}</option>`;
      }
    );
    $$payload2.out += `<!---->`;
  }
  $$payload2.out += `<!--]--></select>`;
  pop();
}
function CreateFunction($$payload2, $$props2) {
  push();
  $$payload2.out += `<div class="grid"><div><input type="text" placeholder="New Function..."></div></div>`;
  pop();
}
function _page($$payload2, $$props2) {
  push();
  const functions2 = FunctionsStore.value;
  const scripts = ScriptsStore.value;
  const attributes2 = mergedAttributes().value?.toSorted((a, b) => a.normalized.localeCompare(b.normalized)) ?? [];
  const sorted = ListingsStore.value.sorting ?? [];
  let selectedListing = void 0;
  let { data } = $$props2;
  data.configuration;
  $$payload2.out += `<div class="grid"><div>`;
  if (scripts) {
    $$payload2.out += "<!--[-->";
    const each_array2 = ensure_array_like(Object.keys(scripts));
    $$payload2.out += `<h3>Scripts</h3> `;
    CreateCustomScript($$payload2, { attributes: attributes2 });
    $$payload2.out += `<!----> <hr> <!--[-->`;
    for (let $$index = 0, $$length = each_array2.length; $$index < $$length; $$index++) {
      let id = each_array2[$$index];
      CustomScript($$payload2, { script: scripts[id], attributes: attributes2 });
      $$payload2.out += `<!----> `;
      ScriptFunctions($$payload2, {
        script: scripts[id],
        attributes: attributes2,
        functions: functions2,
        listing: selectedListing
      });
      $$payload2.out += `<!----> <hr>`;
    }
    $$payload2.out += `<!--]-->`;
  } else {
    $$payload2.out += "<!--[!-->";
  }
  $$payload2.out += `<!--]--></div> <div><h3>Example Listing</h3> `;
  ListingSearch($$payload2, {
    sorted
  });
  $$payload2.out += `<!----> `;
  {
    $$payload2.out += "<!--[!-->";
  }
  $$payload2.out += `<!--]--></div></div> <div class="grid"><div>`;
  if (functions2) {
    $$payload2.out += "<!--[-->";
    const each_array_1 = ensure_array_like(Object.keys(functions2));
    $$payload2.out += `<h3>Functions</h3> `;
    CreateFunction($$payload2);
    $$payload2.out += `<!----> <hr> <!--[-->`;
    for (let $$index_1 = 0, $$length = each_array_1.length; $$index_1 < $$length; $$index_1++) {
      let id = each_array_1[$$index_1];
      Function($$payload2, { fun: functions2[id] });
    }
    $$payload2.out += `<!--]-->`;
  } else {
    $$payload2.out += "<!--[!-->";
  }
  $$payload2.out += `<!--]--></div></div>`;
  pop();
}
export {
  _page as default
};
