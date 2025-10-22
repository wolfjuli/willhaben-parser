import * as universal from '../entries/pages/_layout.ts.js';

export const index = 0;
let component_cache;
export const component = async () => component_cache ??= (await import('../entries/pages/_layout.svelte.js')).default;
export { universal };
export const universal_id = "src/routes/+layout.ts";
export const imports = ["_app/immutable/nodes/0.smks52Wp.js","_app/immutable/chunks/CYEyA5r_.js","_app/immutable/chunks/B17x10Mu.js","_app/immutable/chunks/CF65HRtN.js","_app/immutable/chunks/f5cgzvLy.js","_app/immutable/chunks/Ci0WnR9e.js","_app/immutable/chunks/7tyWikOm.js","_app/immutable/chunks/DwEdY2hh.js","_app/immutable/chunks/DYseaSKU.js","_app/immutable/chunks/CJIEG0_P.js","_app/immutable/chunks/zNrkdR33.js"];
export const stylesheets = ["_app/immutable/assets/0.nZAfvHvV.css","_app/immutable/assets/grid.A1STO8mY.css"];
export const fonts = [];
