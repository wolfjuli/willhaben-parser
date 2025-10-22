export const manifest = (() => {
function __memo(fn) {
	let value;
	return () => value ??= (value = fn());
}

return {
	appDir: "_app",
	appPath: "{basename}/_app",
	assets: new Set([".htaccess","dark.css","favicon.png","light.css"]),
	mimeTypes: {".css":"text/css",".png":"image/png"},
	_: {
		client: {start:"_app/immutable/entry/start.DCqRQIKz.js",app:"_app/immutable/entry/app.4V01NWPx.js",imports:["_app/immutable/entry/start.DCqRQIKz.js","_app/immutable/chunks/Bcho8Ufm.js","_app/immutable/chunks/B17x10Mu.js","_app/immutable/chunks/Bgqb26Hr.js","_app/immutable/entry/app.4V01NWPx.js","_app/immutable/chunks/B17x10Mu.js","_app/immutable/chunks/BVnXvrre.js","_app/immutable/chunks/CF65HRtN.js","_app/immutable/chunks/CYEyA5r_.js","_app/immutable/chunks/f5cgzvLy.js","_app/immutable/chunks/BDxdC6x7.js","_app/immutable/chunks/Bgqb26Hr.js"],stylesheets:[],fonts:[],uses_env_dynamic_public:false},
		nodes: [
			__memo(() => import('./nodes/0.js')),
			__memo(() => import('./nodes/1.js')),
			__memo(() => import('./nodes/2.js')),
			__memo(() => import('./nodes/3.js')),
			__memo(() => import('./nodes/4.js')),
			__memo(() => import('./nodes/5.js')),
			__memo(() => import('./nodes/6.js')),
			__memo(() => import('./nodes/7.js')),
			__memo(() => import('./nodes/8.js'))
		],
		routes: [
			{
				id: "/",
				pattern: /^\/$/,
				params: [],
				page: { layouts: [0,], errors: [1,], leaf: 5 },
				endpoint: null
			},
			{
				id: "/attributes",
				pattern: /^\/attributes\/?$/,
				params: [],
				page: { layouts: [0,2,], errors: [1,,], leaf: 6 },
				endpoint: null
			},
			{
				id: "/functions",
				pattern: /^\/functions\/?$/,
				params: [],
				page: { layouts: [0,3,], errors: [1,,], leaf: 7 },
				endpoint: null
			},
			{
				id: "/settings",
				pattern: /^\/settings\/?$/,
				params: [],
				page: { layouts: [0,4,], errors: [1,,], leaf: 8 },
				endpoint: null
			}
		],
		prerendered_routes: new Set([]),
		matchers: async () => {
			
			return {  };
		},
		server_assets: {}
	}
}
})();
