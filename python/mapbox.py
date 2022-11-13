import json
import time

import requests
import defintions

from python.data.structs import LatLong, Distance

with open(f"{defintions.base_path}/mapbox-api-key", 'r') as f:
    access_token = f.read()

st_peter_hauptstrasse = LatLong(47.0751271, 15.4527669)
merangasse = LatLong(47.041213, 15.488587,)
graz_hauptplatz = LatLong(47.0711759, 15.4383532, "Graz Hauptplatz")
soeding = LatLong(47.011235, 15.274204, "Söding")

session = requests.session()


def __mapbox_url(*coords: LatLong):
    cs = ";".join([f"{c.long},{c.lat}" for c in coords])
    return f"https://api.mapbox.com/directions-matrix/v1/mapbox/driving/{cs}?annotations=distance&access_token={access_token}"


distances = {}


def distance(from_coord, to_coords):
    unknown = [to_coord for to_coord in to_coords if (from_coord, to_coord) not in distances]
    if len(unknown) > 0:
        while True:
            raw = json.loads(session.get(__mapbox_url(from_coord, *unknown)).text)
            if 'message' in raw and "Too Many" in raw['message']:
                print("Mapbox is not happy... going to sleep for 30 seconds")
                time.sleep(30)
                continue

            break

        for i, to in enumerate(unknown):
            distances[(from_coord, to)] = raw['distances'][0][i + 1]

    return [Distance(
        from_coord,
        to,
        distances[(from_coord, to)]
    ) for to in to_coords]
