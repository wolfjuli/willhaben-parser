import * as universal from '../entries/pages/_page.ts.js';

export const index = 5;
let component_cache;
export const component = async () => component_cache ??= (await import('../entries/pages/_page.svelte.js')).default;
export { universal };
export const universal_id = "src/routes/+page.ts";
export const imports = ["_app/immutable/nodes/5.De-T2gXa.js","_app/immutable/chunks/Bcho8Ufm.js","_app/immutable/chunks/B17x10Mu.js","_app/immutable/chunks/Bgqb26Hr.js","_app/immutable/chunks/CYEyA5r_.js","_app/immutable/chunks/7tyWikOm.js","_app/immutable/chunks/CF65HRtN.js","_app/immutable/chunks/f5cgzvLy.js","_app/immutable/chunks/BWdA4GzL.js","_app/immutable/chunks/BVnXvrre.js","_app/immutable/chunks/DKBOVYmS.js","_app/immutable/chunks/B28m_p6u.js","_app/immutable/chunks/Ci0WnR9e.js","_app/immutable/chunks/DwEdY2hh.js","_app/immutable/chunks/DYseaSKU.js","_app/immutable/chunks/LModzcTT.js","_app/immutable/chunks/CNI6ksvf.js"];
export const stylesheets = ["_app/immutable/assets/settings.Bp-lUICt.css","_app/immutable/assets/ListingDetail.DDQOyWEZ.css"];
export const fonts = [];
