class Initializer {
    private funs: (() => void)[] = []

    add(fun: () => void) {
        this.funs.push(fun)
    }

    initialize() {
        this.funs.forEach(f => f())
    }
}

export const initializer = new Initializer()
