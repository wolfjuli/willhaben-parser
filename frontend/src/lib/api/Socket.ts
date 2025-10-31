export class Socket {
    // @ts-ignore
    private ws: WebSocket
    private ready: boolean = false
    private handlers: { [type: string]: ((data: any) => void) } = {}
    private specializedHandlers: { [type: string]: (data: any) => void } = {}

    private initWebsocket() {
        this.ws = new WebSocket("/api/v1/ws")
        this.ws.onopen = async (): Promise<void> => {
            this.ready = true;
        }
        this.ws.onmessage = Socket.handleWSMessage
        this.ws.onclose = () => {
            this.initWebsocket();
        }
        this.ws.onerror = (event) => {
            console.error("Websocket error", event)
        }
    }

    constructor() {
        this.initWebsocket()
    }


    private static _instance: Socket | undefined = undefined
    private static get instance(): Socket {
        if (!Socket._instance) {
            Socket._instance = new Socket()
        }
        return Socket._instance!!
    }

    static send<T>(type: string,
                   data: { [k: string]: any } = {},
                   onReceive: ((data: T) => void) | undefined = undefined
    ) {
        const id = new Date().valueOf()
        if (onReceive) Socket.instance.specializedHandlers[id] = onReceive

        const action = (type: string, data: { [k: string]: any } = {}) => {
            Socket.instance.ws.send(JSON.stringify({type, id, ...data}))
        }

        if (!Socket.instance.ready) {
            setTimeout(() => action(type, data), 100)
        } else {
            action(type, data)
        }

    }

    private static handleWSMessage(event: WebSocketEventMap['message']) {
        let body: { type: string, id: number,  data: unknown } | { type: 'error', id: number } = JSON.parse(event.data);
        if (body.type === 'error') return console.error(body)


        const handler = Socket.instance.handlers[body.type]
        const special = Socket.instance.specializedHandlers[body.id]
        if (!handler && !special) return console.error("Cannot handle message ", body)

        if(handler)
            handler((body as { type: string, data: unknown }).data)

        if(special) {
            delete Socket.instance.specializedHandlers[body.id]
            special((body as { type: string, data: unknown }).data)
        }
    }

    static register<T>(type: string, action: (data: T) => void) {
        Socket.instance.handlers[type] = action
    }
}
