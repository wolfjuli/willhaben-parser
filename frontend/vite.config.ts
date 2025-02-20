import {svelteTesting} from '@testing-library/svelte/vite'
import {sveltekit} from '@sveltejs/kit/vite'
import {defineConfig} from 'vite'
import circularDependency from 'vite-plugin-circular-dependency'

export default defineConfig({
    plugins: [
        sveltekit(),
        circularDependency({
            exclude: [/node_modules/, /\.git/],
        }),
    ],
    server: {
        port: 3000,
        proxy: {
            '/api/rest/v1': {
                target: 'http://localhost:3999/',
                changeOrigin: true,
                secure: false,
            },
            '^/assets/[^/]+/.*': {
                target: 'http://localhost:3999/',
                changeOrigin: true,
                secure: false,
            },
        },
    },
    test: {
        workspace: [
            {
                extends: './vite.config.ts',
                plugins: [svelteTesting()],

                test: {
                    name: 'client',
                    environment: 'jsdom',
                    clearMocks: true,
                    include: ['src/**/*.svelte.{test,spec}.{js,ts}'],
                    exclude: ['src/lib/server/**'],
                    setupFiles: ['./vitest-setup-client.ts'],
                },
            },
            {
                extends: './vite.config.ts',

                test: {
                    name: 'server',
                    environment: 'node',
                    include: ['src/**/*.{test,spec}.{js,ts}'],
                    exclude: ['src/**/*.svelte.{test,spec}.{js,ts}'],
                },
            },
        ],
    },
})
