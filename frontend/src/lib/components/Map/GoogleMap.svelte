<script lang="ts">
	import { Loader } from '@googlemaps/js-api-loader'
	import { onMount } from 'svelte'
	import type {   MapComponent } from './Map'
	//import { toAddress, toMarker } from './transformers'

	let { onaddress = () => {}, onclick = () => {}, pois = [] }: MapComponent = $props()

	let element: HTMLDivElement

	onMount(start)

	let map: google.maps.Map
	let geocoder: google.maps.Geocoder

	async function start() {
		const loader = new Loader({
			apiKey: 'AIzaSyCMDGyMDaKU8IBLbtr3D71cpztYoG-wpxg',
			version: 'weekly',
			libraries: ['maps', 'geocoding'],
			authReferrerPolicy: undefined,
		})
		let { Map } = await loader.importLibrary('maps')
		let { Geocoder } = await loader.importLibrary('geocoding')

		map = new Map(element, {
			center: { lat: 47.063311728992616, lng: 15.437637197786666 },
			zoom: 12,
			mapId: 'mapid',
			disableDefaultUI: false,
		})

		geocoder = new Geocoder()

		//pois.map (p => toMarker(map, p))

		map.addListener('click', async (mapMouseEvent: google.maps.MapMouseEvent) => {
			const [lat, lng] = [mapMouseEvent.latLng?.lat(), mapMouseEvent.latLng?.lng()]
			onclick({lat, lng})
            
            if(!lat || !lng)
                return console.error("Something went wrong when retrieving lat/lng: ", lat, lng)

			const resp: google.maps.GeocoderResponse = await geocoder.geocode({ location: { lat, lng } })
				
			const rawAddress = resp.results[0] 
			if(!rawAddress)	return

			const address: Address = toAddress(resp.results[0])
		})
	}
</script>

<div id="map" bind:this={element}></div>


<style>
	#map {
		width: 60vw;
		height: 80vh;
	}
</style>
