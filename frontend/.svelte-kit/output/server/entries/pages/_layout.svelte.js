import { j as sanitize_props, k as spread_attributes, l as head, h as pop, p as push, m as attr } from "../../chunks/index2.js";
import { h as html } from "../../chunks/html.js";
import "bootstrap";
/* empty css                    */
import { L as ListingsStore, F as FetchingStore } from "../../chunks/ListingsStore.svelte.js";
import { S as ScriptsStore } from "../../chunks/ScriptsStore.svelte.js";
function createSubscriber(_) {
  return () => {
  };
}
const LOCAL_STORAGE_KEY = "color-scheme";
const DARK = "dark";
const LIGHT = "light";
const getStoredScheme = () => {
  const scheme = window.localStorage.getItem(LOCAL_STORAGE_KEY);
  if (scheme === LIGHT) return LIGHT;
  if (scheme === DARK) return DARK;
};
const mediaList = window.matchMedia?.("(prefers-color-scheme: dark)");
class Scheme {
  #subscribe;
  #systemScheme = mediaList.matches ? DARK : LIGHT;
  #storedScheme = getStoredScheme();
  // undefined = user has no preference
  constructor() {
    this.#subscribe = createSubscriber();
  }
  get current() {
    this.#subscribe();
    return this.#storedScheme ?? this.#systemScheme;
  }
  switch() {
    this.#update(this.current === DARK ? LIGHT : DARK);
  }
  #update(scheme) {
    this.#storedScheme = this.#systemScheme === scheme ? void 0 : scheme;
  }
}
function Gear_svg_component($$payload, $$props) {
  const $$sanitized_props = sanitize_props($$props);
  $$payload.out += `<svg${spread_attributes(
    {
      xmlns: "http://www.w3.org/2000/svg",
      width: "24",
      height: "24",
      viewBox: "0 -960 960 960",
      ...$$sanitized_props
    },
    void 0,
    void 0,
    3
  )}>${html('<path d="m370-80-16-128q-13-5-24.5-12T307-235l-119 50L78-375l103-78q-1-7-1-13.5v-27q0-6.5 1-13.5L78-585l110-190 119 50q11-8 23-15t24-12l16-128h220l16 128q13 5 24.5 12t22.5 15l119-50 110 190-103 78q1 7 1 13.5v27q0 6.5-2 13.5l103 78-110 190-118-50q-11 8-23 15t-24 12L590-80zm70-80h79l14-106q31-8 57.5-23.5T639-327l99 41 39-68-86-65q5-14 7-29.5t2-31.5-2-31.5-7-29.5l86-65-39-68-99 42q-22-23-48.5-38.5T533-694l-13-106h-79l-14 106q-31 8-57.5 23.5T321-633l-99-41-39 68 86 64q-5 15-7 30t-2 32q0 16 2 31t7 30l-86 65 39 68 99-42q22 23 48.5 38.5T427-266zm42-180q58 0 99-41t41-99-41-99-99-41q-59 0-99.5 41T342-480t40.5 99 99.5 41m-2-140"/>')}</svg>`;
}
function Loading_svg_component($$payload, $$props) {
  const $$sanitized_props = sanitize_props($$props);
  $$payload.out += `<svg${spread_attributes(
    {
      xmlns: "http://www.w3.org/2000/svg",
      width: "24",
      height: "24",
      viewBox: "0 -960 960 960",
      ...$$sanitized_props
    },
    void 0,
    void 0,
    3
  )}>${html('<path d="m370-80-16-128q-13-5-24.5-12T307-235l-119 50L78-375l103-78q-1-7-1-13.5v-27q0-6.5 1-13.5L78-585l110-190 119 50q11-8 23-15t24-12l16-128h220l16 128q13 5 24.5 12t22.5 15l119-50 110 190-103 78q1 7 1 13.5v27q0 6.5-2 13.5l103 78-110 190-118-50q-11 8-23 15t-24 12L590-80zm70-80h79l14-106q31-8 57.5-23.5T639-327l99 41 39-68-86-65q5-14 7-29.5t2-31.5-2-31.5-7-29.5l86-65-39-68-99 42q-22-23-48.5-38.5T533-694l-13-106h-79l-14 106q-31 8-57.5 23.5T321-633l-99-41-39 68 86 64q-5 15-7 30t-2 32q0 16 2 31t7 30l-86 65 39 68 99-42q22 23 48.5 38.5T427-266zm42-180q58 0 99-41t41-99-41-99-99-41q-59 0-99.5 41T342-480t40.5 99 99.5 41m-2-140"/><animateTransform attributeName="transform" calcMode="spline" dur="2" keySplines="0 0 1 1" keyTimes="0;1" repeatCount="indefinite" type="rotate" values="0;120"/>')}</svg>`;
}
class Initializer {
  funs = [];
  add(fun) {
    this.funs.push(fun);
  }
  initialize() {
    this.funs.forEach((f) => f());
  }
}
const initializer = new Initializer();
function _layout($$payload, $$props) {
  push();
  let { children } = $$props;
  const scheme = new Scheme();
  ListingsStore.instance;
  ScriptsStore.instance;
  navigator && navigator.storage && navigator.storage.persist();
  initializer.initialize();
  head($$payload, ($$payload2) => {
    $$payload2.out += `<link rel="stylesheet"${attr("href", `${scheme.current}.css`)}>`;
  });
  $$payload.out += `<nav class="svelte-j0st2u"><ul><li><strong>WillHaben Parser</strong></li> <li><a href="/settings">`;
  if (FetchingStore.fetching) {
    $$payload.out += "<!--[-->";
    Loading_svg_component($$payload, {});
  } else {
    $$payload.out += "<!--[!-->";
    Gear_svg_component($$payload, {});
  }
  $$payload.out += `<!--]--></a></li></ul> <ul><li><a href="/">Home</a></li> <li><a href="/functions">Functions</a></li> <li><a href="/attributes">Attributes</a></li></ul></nav> <main class="container-fluid">`;
  children($$payload);
  $$payload.out += `<!----></main>`;
  pop();
}
export {
  _layout as default
};
