import {handleWSMessage} from "$lib/api/handleWSMessage";

export class Socket {
    private ws: WebSocket
    private ready : boolean = false;

    private initWebsocket () {
        console.log("Init WebSocket");
        this.ws =  new WebSocket("/api/v1/ws")
        this.ws.onopen = async (): Promise<void> => {
            console.log("WS opened")
            this.ready = true;
        }
        this.ws.onmessage = handleWSMessage
        this.ws.onclose = () => {
            console.log("Connection closed")
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
        if(!this._instance) {
            this._instance = new Socket()
        }
        return this._instance!!
    }

    static send(type: string, data: { [k: string]: any} = {}): void {
        if(!this.instance.ready) {
            setTimeout(() => this.send(type, data), 100)
            return
        }
        this.instance.ws.send(JSON.stringify({type, ...data}))
    }
}
