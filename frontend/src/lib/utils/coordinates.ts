import type {LatLng} from "$lib/types/LatLng";

export function toEpsg3857(coordinates: LatLng): LatLng {
    if (coordinates.epsg !== 4326)
        throw new Error("Invalid coordinate system " + coordinates)
    let lng = (coordinates.lng * 20037508.34) / 180
    let lat = Math.log(Math.tan(((90 + coordinates.lng) * Math.PI) / 360)) / (Math.PI / 180)

    lat = (lat * 20037508.34) / 180
    return {lng, lat, epsg: 3857};
}

export function toEpsg4326(coordinates: LatLng): LatLng {
    if (coordinates.epsg !== 3587)
        throw new Error("Invalid coordinate system " + coordinates)
    let lng = (coordinates.lng * 180) / 20037508.34;
    let lat = (coordinates.lat * 180) / 20037508.34;
    lat = (Math.atan(Math.pow(Math.E, lat * (Math.PI / 180))) * 360) / Math.PI - 90;
    return {lng, lat, epsg: 4326};
}

export function boxAroundEpsg4326(coordinates: LatLng, radius: number = 50): [LatLng, LatLng] {
    if (coordinates.epsg !== 4326)
        throw new Error("Invalid coordinate system " + coordinates)

    const lngStep = 0.00107 / 100 * radius
    const latStep = 0.000898 / 100 * radius

    return [
        {lng: coordinates.lng - lngStep, lat: coordinates.lat - latStep, epsg: 4326},
        {lng: coordinates.lng + lngStep, lat: coordinates.lat + latStep, epsg: 4326}
    ]
}