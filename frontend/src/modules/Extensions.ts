let debugEnabled: boolean = true


export function logDebug(...obj: any) {
    if (debugEnabled)
        console.log(...obj)
}

export function logError(...obj: any) {
    console.error(...obj)
}