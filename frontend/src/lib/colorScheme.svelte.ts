import {createSubscriber} from 'svelte/reactivity'

const LOCAL_STORAGE_KEY = 'color-scheme'

const DARK = 'dark'
const LIGHT = 'light'

type SchemeType = typeof DARK | typeof LIGHT

const getStoredScheme = () => {
    const scheme = window.localStorage.getItem(LOCAL_STORAGE_KEY)
    if (scheme === LIGHT) return LIGHT
    if (scheme === DARK) return DARK
}

const updateLocalStore = (newScheme?: SchemeType) => {
    window.localStorage.removeItem(LOCAL_STORAGE_KEY)
    if (newScheme) window.localStorage.setItem(LOCAL_STORAGE_KEY, newScheme)
}

const updateClassName = (newScheme?: SchemeType) => {
    document.documentElement.classList.remove(DARK, LIGHT)
    if (newScheme !== undefined) document.documentElement.classList.add(newScheme)
}

const mediaList = window.matchMedia?.('(prefers-color-scheme: dark)')

export class Scheme {
    #subscribe

    #systemScheme: SchemeType = $state(mediaList.matches ? LIGHT : DARK)
    #storedScheme = $state<SchemeType | undefined>(getStoredScheme()) // undefined = user has no preference

    constructor() {
        const updateSystemScheme = () => (this.#systemScheme = mediaList.matches ? DARK : LIGHT)

        this.#subscribe = createSubscriber(() => {
            mediaList.addEventListener('change', updateSystemScheme)
            return () => mediaList.removeEventListener('change', updateSystemScheme)
        })

        $effect(() => {
            updateLocalStore(this.#storedScheme)
            updateClassName(this.#storedScheme)
        })
    }

    get current() {
        this.#subscribe()
        return this.#storedScheme ?? this.#systemScheme
    }

    switch() {
        this.#update(this.current === DARK ? LIGHT : DARK)
    }

    #update(scheme: SchemeType) {
        this.#storedScheme = this.#systemScheme === scheme ? undefined : scheme
    }
}
