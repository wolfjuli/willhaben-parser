import { o as ensure_array_like, m as attr, n as escape_html, h as pop, p as push } from "./index2.js";
function Dropdown($$payload, $$props) {
  push();
  let {
    onchange = () => false,
    values,
    nameSelector = (v) => v.id,
    emptyFirstLineText = "Select...",
    preSelected
  } = $$props;
  const each_array = ensure_array_like(values.toSorted((a, b) => nameSelector(a).localeCompare(nameSelector(b))));
  $$payload.out += `<select>`;
  if (emptyFirstLineText !== void 0) {
    $$payload.out += "<!--[-->";
    $$payload.out += `<option disabled${attr("selected", true, true)}${attr("value", emptyFirstLineText)}>${escape_html(emptyFirstLineText)}</option>`;
  } else {
    $$payload.out += "<!--[!-->";
  }
  $$payload.out += `<!--]--><!--[-->`;
  for (let $$index = 0, $$length = each_array.length; $$index < $$length; $$index++) {
    let v = each_array[$$index];
    $$payload.out += `<option${attr("value", v.id)}${attr("selected", v.id === preSelected, true)}>${escape_html(nameSelector(v))}</option>`;
  }
  $$payload.out += `<!--]--></select>`;
  pop();
}
export {
  Dropdown as D
};
