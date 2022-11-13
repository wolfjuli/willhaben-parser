import type {Subscriber} from "svelte/types/runtime/store";

let debugEnabled: boolean = true


export function logDebug(...obj: any) {
    if (debugEnabled)
        console.log(...obj)
}

export function logError(...obj: any) {
    console.error(...obj)
}


export function trickleCopy<T>(sourceList: T[], callbacks: Subscriber<T[]>[], steps = 20): NodeJS.Timer {

    let currentStep = 0
    let targetList = []

    let timer: NodeJS.Timer = setInterval(() => {
        if (currentStep > sourceList.length)
            clearInterval(timer)

        targetList.push(...sourceList.slice(currentStep, currentStep + steps))
        callbacks.forEach(c => c(targetList))

        currentStep += steps
    }, 10)

    return timer
}