import adapter from '@sveltejs/adapter-static'
import {vitePreprocess} from '@sveltejs/vite-plugin-svelte'

const base = process.env.NODE_ENV === 'production' ? '/{basename}' : ''

/** @type {import('@sveltejs/kit').Config} */
const config = {
    // Consult https://svelte.dev/docs/kit/integrations
    // for more information about preprocessors
    preprocess: vitePreprocess(),

    kit: {
        // adapter-auto only supports some environments, see https://svelte.dev/docs/kit/adapter-auto for a list.
        // If your environment is not supported, or you settled on a specific environment, switch out the adapter.
        // See https://svelte.dev/docs/kit/adapters for more information about adapters.
        adapter: adapter({
            fallback: 'index.html',
            pages: 'dist',
        }),
        paths: {
            base,
            relative: true,
        },
        csp: {
            directives: {
                'script-src': ['self', 'maps.googleapis.com', 'unsafe-eval'],
                'connect-src': ['self', 'maps.googleapis.com']
            }
        },
        alias: {
            $houdini: './$houdini',
            $fonts: './src/lib/assets/fonts',
            $images: './src/lib/assets/images',
            $styles: './src/lib/assets/styles',
        },
    },
}

export default config
