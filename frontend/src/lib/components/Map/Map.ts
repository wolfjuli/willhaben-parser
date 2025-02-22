export interface MapComponent {
    pois: LatLng[]
    onclick: (LatLng) => void
    onaddress: (Address) => void
}
