function AsSingleton<T extends object>(ctor: new () => T): { instance: T } {
    return (() => {
        let instance: T | undefined;
        return {
            get instance(): T {
                if (!instance) {
                    instance = new ctor()
                }
                return instance
            }
        }
    })();
}
