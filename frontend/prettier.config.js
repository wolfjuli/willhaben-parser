import inveniumPrettierConfig from '@invenium/prettier-config'

export default {
    ...inveniumPrettierConfig,
    plugins: [
        'prettier-plugin-css-order',
        'prettier-plugin-svelte',
        'prettier-plugin-packagejson',
        '@awmottaz/prettier-plugin-void-html',
    ],
    useTabs: true,
    semi: false,
    bracketSameLine: false,
    singleAttributePerLine: false,
    overrides: [{files: '*.svelte', options: {parser: 'svelte', svelteStrictMode: true}}],
}
