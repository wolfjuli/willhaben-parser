// import { Address } from '../../types/address';
// import { LatLng } from '../../types/latlng';

export function toAddress(response: google.maps.GeocoderResult): Address {
    return {
        formatted: rawAddress.formatted_address,
        latLng: {lat, lng},
        streetNumber: rawAddress.address_components.find(c => c.types.find(t => t === "street_number"))?.long_name,
        road: rawAddress.address_components.find(c => c.types.find(t => t === "route"))?.long_name,
        city: rawAddress.address_components.find(c => c.types.find(t => t === "locality"))?.long_name,
        postalCode: rawAddress.address_components.find(c => c.types.find(t => t === "postal_code"))?.short_name,
        district: rawAddress.address_components.find(c => c.types.find(t => t === "sublocality" || t === "administrative_area_level_2"))?.long_name,
        country: rawAddress.address_components.find(c => c.types.find(t => t === "country"))?.long_name,
        plusCode: rawAddress.plus_code?.global_code,
        placeId: rawAddress.place_id
    }
}
