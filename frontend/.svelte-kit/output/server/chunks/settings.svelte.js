import { o as ensure_array_like, m as attr, u as element, j as sanitize_props, k as spread_attributes, v as bind_props, p as push, h as pop, n as escape_html, w as stringify } from "./index2.js";
import { h as html } from "./html.js";
import { p as page } from "./index3.js";
/* empty css                                            */
import { L as ListingsStore } from "./ListingsStore.svelte.js";
import { L as ListingFilter } from "./ListingFilter.js";
function Table($$payload, $$props) {
  let { tableData, thead, row } = $$props;
  const each_array = ensure_array_like(tableData);
  $$payload.out += `<table class="striped">`;
  if (thead) {
    $$payload.out += "<!--[-->";
    $$payload.out += `<thead>`;
    thead($$payload);
    $$payload.out += `<!----></thead>`;
  } else {
    $$payload.out += "<!--[!-->";
  }
  $$payload.out += `<!--]--><tbody><!--[-->`;
  for (let idx = 0, $$length = each_array.length; idx < $$length; idx++) {
    let obj = each_array[idx];
    row($$payload, obj, idx);
    $$payload.out += `<!---->`;
  }
  $$payload.out += `<!--]--></tbody></table>`;
}
function TD($$payload, $$props) {
  let {
    colspan = 1,
    right = false,
    onclick = void 0,
    render
  } = $$props;
  $$payload.out += `<td${attr("colspan", colspan)}${attr("class", [right ? "right" : ""].filter(Boolean).join(" "))}>`;
  element($$payload, onclick ? "button" : "div", void 0, () => {
    render($$payload);
    $$payload.out += `<!---->`;
  });
  $$payload.out += `</td>`;
}
function Arrow_drop_down_svg_component($$payload, $$props) {
  const $$sanitized_props = sanitize_props($$props);
  $$payload.out += `<svg${spread_attributes(
    {
      xmlns: "http://www.w3.org/2000/svg",
      viewBox: "0 -610 960 300",
      ...$$sanitized_props
    },
    void 0,
    void 0,
    3
  )}>${html('<path d="M480-360 280-560h400z"/>')}</svg>`;
}
function Arrow_drop_up_svg_component($$payload, $$props) {
  const $$sanitized_props = sanitize_props($$props);
  $$payload.out += `<svg${spread_attributes(
    {
      xmlns: "http://www.w3.org/2000/svg",
      viewBox: "0 -650 960 300",
      ...$$sanitized_props
    },
    void 0,
    void 0,
    3
  )}>${html('<path d="m280-400 200-200 200 200z"/>')}</svg>`;
}
function TableSort($$payload, $$props) {
  let color;
  let sorted = $$props["sorted"];
  let sortAscending = $$props["sortAscending"];
  color = sorted ? void 0 : "gray";
  if (sortAscending) {
    $$payload.out += "<!--[-->";
    $$payload.out += `<md-icon class="svelte-1cekuc8">`;
    Arrow_drop_up_svg_component($$payload, { color });
    $$payload.out += `<!----> `;
    Arrow_drop_down_svg_component($$payload, { color: "gray" });
    $$payload.out += `<!----></md-icon>`;
  } else {
    $$payload.out += "<!--[!-->";
    $$payload.out += `<md-icon class="svelte-1cekuc8">`;
    Arrow_drop_up_svg_component($$payload, { color: "gray" });
    $$payload.out += `<!----> `;
    Arrow_drop_down_svg_component($$payload, { color });
    $$payload.out += `<!----></md-icon>`;
  }
  $$payload.out += `<!--]-->`;
  bind_props($$props, { sorted, sortAscending });
}
function TH($$payload, $$props) {
  push();
  let {
    label,
    currentColumn,
    sorted = false,
    sortAscending = false,
    onSort = void 0
  } = $$props;
  let sortable = !!onSort;
  const sortDirection = sortAscending ? "ascending" : "descending";
  $$payload.out += `<th${attr("aria-sort", sorted ? sortDirection : void 0)} class="svelte-if2pnl">`;
  element(
    $$payload,
    sortable ? "button" : "div",
    () => {
      $$payload.out += `${attr("role", sortable ? "button" : "")} class="svelte-if2pnl"`;
    },
    () => {
      $$payload.out += `<i-th-content class="svelte-if2pnl">${escape_html(label)} `;
      if (sortable) {
        $$payload.out += "<!--[-->";
        TableSort($$payload, { sorted, sortAscending });
      } else {
        $$payload.out += "<!--[!-->";
      }
      $$payload.out += `<!--]--></i-th-content>`;
    }
  );
  $$payload.out += `</th>`;
  pop();
}
function ListingValue($$payload, $$props) {
  push();
  let {
    listing,
    attribute,
    configuration,
    onclick = () => {
    },
    ondblclick = () => {
    }
  } = $$props;
  let attr$1 = listing && attribute ? listing[attribute.normalized] : void 0;
  let val = attr$1?.base?.toString() ?? "[empty]";
  let userVal = attr$1?.user?.toString();
  let obj = attr$1?.custom;
  $$payload.out += `<span>`;
  if (attribute.dataType === "LINK") {
    $$payload.out += "<!--[-->";
    if (obj && obj.href) {
      $$payload.out += "<!--[-->";
      $$payload.out += `<a${attr("href", configuration.listingsBaseUrl + "/" + obj.href)} target="_blank">${escape_html(obj.value)}</a>`;
    } else {
      $$payload.out += "<!--[!-->";
      $$payload.out += `<a${attr("href", configuration.listingsBaseUrl + `/${val}`)} target="_blank">${escape_html(val)}</a>`;
    }
    $$payload.out += `<!--]-->`;
  } else {
    $$payload.out += "<!--[!-->";
    if (attribute.dataType === "IMAGE") {
      $$payload.out += "<!--[-->";
      $$payload.out += `<img${attr("src", configuration.imageBaseUrl + `/${val}`)}${attr("alt", val)} class="svelte-1i53pa4">`;
    } else {
      $$payload.out += "<!--[!-->";
      $$payload.out += `<div><span${attr("class", `svelte-1i53pa4 ${stringify([userVal ? "strike" : ""].filter(Boolean).join(" "))}`)}>`;
      if (attribute.normalized === "points" || attribute.normalized === "willhabenId") {
        $$payload.out += "<!--[-->";
        $$payload.out += `${escape_html(attr$1)}`;
      } else {
        $$payload.out += "<!--[!-->";
        $$payload.out += `${escape_html(val)}`;
      }
      $$payload.out += `<!--]--></span> `;
      if (userVal) {
        $$payload.out += "<!--[-->";
        $$payload.out += `${escape_html(userVal)}`;
      } else {
        $$payload.out += "<!--[!-->";
      }
      $$payload.out += `<!--]--></div>`;
    }
    $$payload.out += `<!--]-->`;
  }
  $$payload.out += `<!--]--></span>`;
  pop();
}
function EditListingValue($$payload, $$props) {
  push();
  let { listing, attribute } = $$props;
  $$payload.out += `<input type="text"${attr("value", listing?.[attribute.normalized]?.user ?? listing[attribute.normalized]?.custom ?? listing[attribute.normalized]?.base)}>`;
  pop();
}
function ListingDetail($$payload, $$props) {
  let {
    listing,
    attributes,
    configuration,
    horizontal = false
  } = $$props;
  const each_array = ensure_array_like(attributes);
  $$payload.out += `<dl class="svelte-90s297"><!--[-->`;
  for (let $$index = 0, $$length = each_array.length; $$index < $$length; $$index++) {
    let attribute = each_array[$$index];
    $$payload.out += `<e:detail${attr("class", `svelte-90s297 ${stringify([horizontal ? "horizontal" : ""].filter(Boolean).join(" "))}`)}><dt>${escape_html(attribute.label)}</dt> <dd>`;
    ListingValue($$payload, { listing, attribute, configuration });
    $$payload.out += `<!----></dd></e:detail>`;
  }
  $$payload.out += `<!--]--></dl>`;
}
function ListingTable($$payload, $$props) {
  push();
  let { sorting, fields, attributes, configuration } = $$props;
  const searchParams = ListingsStore.value.searchParams;
  let sortAscending = searchParams.sortDir === "ASC";
  let sortKey = "";
  let p = +(page.url.searchParams.get("page") ?? 1);
  let tableData = [];
  sorting.slice((p - 1) * 100, p * 100);
  (/* @__PURE__ */ new Date()).valueOf();
  const onSort = (key) => {
    if (searchParams.sortCol === key) {
      searchParams.sortDir = sortAscending ? "DESC" : "ASC";
    } else {
      searchParams.sortCol = key;
      searchParams.sortDir = "ASC";
    }
    ListingsStore.instance.fetchSorting();
  };
  let editing = { listingId: -1, attributeId: -1 };
  let expanded = [];
  if (expanded && fields) {
    $$payload.out += "<!--[-->";
    $$payload.out += `<div class="container-fluid"><div class="col"><button${attr("disabled", p <= 1, true)}>&lt;</button> Page ${escape_html(p)} <button${attr("disabled", (tableData ?? []).length < 100, true)}>></button></div> <div class="col">`;
    ListingFilter($$payload, { attributes });
    $$payload.out += `<!----></div></div> `;
    {
      let thead = function($$payload2) {
        const each_array = ensure_array_like(fields);
        TH($$payload2, {});
        $$payload2.out += `<!----> <!--[-->`;
        for (let $$index = 0, $$length = each_array.length; $$index < $$length; $$index++) {
          let field = each_array[$$index];
          TH($$payload2, {
            currentColumn: field.normalized,
            label: field.label,
            sorted: sortKey === field.normalized,
            sortAscending,
            onSort
          });
        }
        $$payload2.out += `<!--]-->`;
      }, row = function($$payload2, listing, idx) {
        const each_array_1 = ensure_array_like(fields);
        $$payload2.out += `<tr${attr("class", [idx % 2 ? "even" : ""].filter(Boolean).join(" "))}>`;
        {
          let render = function($$payload3) {
            $$payload3.out += `<button>V</button>`;
          };
          TD($$payload2, { render });
        }
        $$payload2.out += `<!----><!--[-->`;
        for (let $$index_1 = 0, $$length = each_array_1.length; $$index_1 < $$length; $$index_1++) {
          let attribute = each_array_1[$$index_1];
          {
            let render = function($$payload3) {
              if (editing.attributeId === attribute.id && editing.listingId === listing.id) {
                $$payload3.out += "<!--[-->";
                EditListingValue($$payload3, { listing, attribute });
              } else {
                $$payload3.out += "<!--[!-->";
                ListingValue($$payload3, {
                  listing,
                  attribute,
                  configuration,
                  ondblclick: () => {
                    editing = {
                      listingId: listing.id,
                      attributeId: attribute.id
                    };
                  }
                });
              }
              $$payload3.out += `<!--]-->`;
            };
            TD($$payload2, { render });
          }
        }
        $$payload2.out += `<!--]--></tr> `;
        if (expanded && expanded.indexOf(listing.id) > -1) {
          $$payload2.out += "<!--[-->";
          $$payload2.out += `<tr>`;
          if (listing.coordinates) {
            $$payload2.out += "<!--[-->";
            const [lat, long] = (listing.coordinates?.user ?? listing.coordinates?.base)?.toString()?.split(",");
            const addr = (listing["address"]?.user ?? listing["address"]?.base)?.toString() ?? "";
            const district = (listing["district"]?.user ?? listing["district"]?.base)?.toString() ?? "";
            const sunUrl1 = `https://voibos.rechenraum.com/voibos/voibos?Datum=06-21-13%3A00&H=10&name=sonnengang&Koordinate=${long.trim()}%2C${lat.trim()}&CRS=4326&Output=Horizont%2CLage%2CTabelle`;
            const sunUrl2 = `https://voibos.rechenraum.com/voibos/voibos?Datum=06-21-13%3A00&H=10&name=sonnengang&Koordinate=${long.trim()}%2C${lat.trim()}&CRS=4326&Output=Formular%2CHorizont%2CLage%2CTabelle`;
            const katasterUrl = `https://kataster.bev.gv.at/#/center/${long.trim()},${lat.trim()}/zoom/17.5/ortho/1/vermv/1`;
            const laermUrl = `https://maps.laerminfo.at/#/cstrasse22_24h/bgrau/a-/q${addr}, ${district}/@${lat.trim()},${long.trim()},17z`;
            $$payload2.out += `<td colspan="2"><a target="_blank"${attr("href", katasterUrl)}>Kataster</a> <iframe${attr("src", katasterUrl)} class="svelte-19s99i1"></iframe></td> <td colspan="2"><a target="_blank"${attr("href", sunUrl2)}>Sonnestand</a> <iframe${attr("src", sunUrl1)} class="suncalc svelte-19s99i1"></iframe></td> <td colspan="2"><a target="_blank"${attr("href", laermUrl)}>Lärm</a> <iframe${attr("src", laermUrl)} class="svelte-19s99i1"></iframe></td>`;
          } else {
            $$payload2.out += "<!--[!-->";
            $$payload2.out += `<td>Kataster</td> <td>Sonnestand</td> <td>Lärm</td>`;
          }
          $$payload2.out += `<!--]--><td${attr("colspan", fields.length - 6)} class="details svelte-19s99i1">`;
          ListingDetail($$payload2, { listing, attributes, configuration });
          $$payload2.out += `<!----></td></tr>`;
        } else {
          $$payload2.out += "<!--[!-->";
        }
        $$payload2.out += `<!--]-->`;
      };
      Table($$payload, {
        tableData,
        thead,
        row
      });
    }
    $$payload.out += `<!---->`;
  } else {
    $$payload.out += "<!--[!-->";
    $$payload.out += `No data available`;
  }
  $$payload.out += `<!--]-->`;
  pop();
}
const defaultValues = {
  listingFields: [
    "mmo",
    "link",
    "points",
    "propertyType",
    "district",
    "address",
    "priceForDisplay",
    "estateSize",
    "price/m2",
    "willhabenId",
    "notes"
  ],
  searchFields: ["heading", "willhabenId"]
};
const settingsStore = { value: defaultValues };
function setSettings(settings) {
  localStorage.setItem("settings", JSON.stringify(settings));
  settingsStore.value = settings;
}
export {
  ListingTable as L,
  setSettings as a,
  settingsStore as s
};
