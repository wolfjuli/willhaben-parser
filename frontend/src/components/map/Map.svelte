<script lang="ts">
    import L from 'leaflet';

    export let latLong: string

    export const icon = `<svg style="width:30px;height:30px" fill="none" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" stroke="currentColor"><path d="M8 14v3m4-3v3m4-3v3M3 21h18M3 10h18M3 7l9-4 9 4M4 10h16v11H4V10z"></path></svg>`;

    const initialView = latLong.split(",").map(n => +n)
    let map

    function createMap(container) {
        let m = L.map(container, {preferCanvas: true}).setView(initialView, 12);
        L.tileLayer(
            'https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png',
            {
                subdomains: 'abcd',
                maxZoom: 25,
            }
        ).addTo(m);

        return m;
    }

    let eye = true;
    let lines = true;

    // let toolbar = L.control({position: 'topright'});
    // let toolbarComponent;


    // Create a popup with a Svelte component inside it and handle removal when the popup is torn down.
    // `createFn` will be called whenever the popup is being created, and should create and return the component.
    function bindPopup(marker, createFn) {
        let popupComponent;
        marker.bindPopup(() => {
            let container = L.DomUtil.create('div');
            popupComponent = createFn(container);
            return container;
        });

        marker.on('popupclose', () => {
            if (popupComponent) {
                let old = popupComponent;
                popupComponent = null;
                // Wait to destroy until after the fadeout completes.
                setTimeout(() => {
                    old.$destroy();
                }, 500);

            }
        });
    }

    let markers = new Map();

    function markerIcon() {
        let html = `<div class="map-marker"><div>${icon}</div><div class="marker-text"></div></div>`;
        return L.divIcon({
            html,
            className: 'map-marker'
        });
    }


    function createMarker(loc) {
        let icon = markerIcon();
        let marker = L.marker(loc, {icon});
        // bindPopup(marker, (m) => {
        //     let c = new MarkerPopup({
        //         target: m,
        //         props: {
        //             count
        //         }
        //     });
        //
        //     c.$on('change', ({detail}) => {
        //         count = detail;
        //         marker.setIcon(markerIcon(count));
        //     });
        //
        //     return c;
        // });

        return marker;
    }

    //
    // function createLines() {
    //     return L.polyline(markerLocations, {color: '#E4E', opacity: 0.5});
    // }

    let markerLayers;
    let lineLayers;

    function mapAction(container) {
        map = createMap(container);
        //toolbar.addTo(map);

        markerLayers = L.layerGroup()
        let m = createMarker(initialView);
        markerLayers.addLayer(m);

        //lineLayers = createLines();
        //
        // markerLayers.addTo(map);
        // lineLayers.addTo(map);

        return {
            destroy: () => {
                //toolbar.remove();
                map.remove();
                map = null;
            }
        };
    }

    // We could do these in the toolbar's click handler but this is an example
    // of modifying the map with reactive syntax.
    $: if (markerLayers && map) {
        if (eye)
            markerLayers.addTo(map);
        else
            markerLayers.remove();

    }

    $: if (lineLayers && map) {
        if (lines)
            lineLayers.addTo(map);
        else
            lineLayers.remove();

    }

    function resizeMap() {
        if (map)
            map.invalidateSize();

    }

</script>
<svelte:window on:resize={resizeMap}/>


<link rel="stylesheet" href="https://unpkg.com/leaflet@1.6.0/dist/leaflet.css"
      integrity="sha512-xwE/Az9zrjBIphAcBb3F6JVqxf46+CDLwfLMHloNu6KEQCAWi6HcDUbeOfBIptF7tcCzusKFjFw2yuvEpDL9wQ=="
      crossorigin=""/>

<div class="map" style="height:100%;width:100%; min-height:40vh" use:mapAction></div>