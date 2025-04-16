import {WithState} from "$lib/stores/WithState.svelte";

export function AsSingleton<T extends object>(ctor: new () => T): { instance: T } {
    const ret=  (() => {
        let instance: T | undefined;
        return {
            get instance(): T {
                if (!instance) {
                    instance = new ctor()
                }
                return instance
            }
        }
    })()

    //Immediately initialize
    ret.instance
    return ret
}

export function SingletonState<V>(initValue: V | undefined = undefined): { value: V } {
    class Empty extends WithState<V>{}
    const ret = (() => {
        let _instance: Empty | undefined;
        return {
            get value(): V {
                if (!_instance) {
                    _instance = new Empty()
                    if(initValue)
                        _instance.value = initValue
                }
                return _instance.value
            },
            set value(v: V) {
                if (!_instance) {
                    _instance = new Empty()
                    if(initValue)
                        _instance.value = initValue
                }
                _instance.value = v
            }
        }
    })()

    //Immediately initialize
    ret.value
    return ret;
}
