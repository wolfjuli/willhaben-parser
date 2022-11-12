import json
import math

import requests
from bs4 import BeautifulSoup

from configuration import Configuration
from defintions import headers

session = requests.Session()
session.headers.update(headers)


def __fetch_next_data(url):
    res = session.get(url)
    # session.get("https://www.willhaben.at/webapi/iad/vertical")
    # session.get("https://www.willhaben.at/webapi/iad/user/me")
    # session.get("https://www.willhaben.at/nodeapi/toggles")

    soup = BeautifulSoup(res.text, "html.parser")
    return json.loads(soup.find("script", {"id": '__NEXT_DATA__'}).text)


# areaId=???
# max 90 results: rows=90
# page=1
def __listing_url(area_ids, page=1):
    area_id_params = "&".join(map(lambda id: f'areaId={id}', area_ids))
    return f"https://www.willhaben.at/iad/immobilien/haus-kaufen/haus-angebote?sfId=528fed80-90c1-4432-92bb-1f14fd8019d3&isNavigation=true&rows=90&{area_id_params}&page={page}"


ids = set()

def get_ids(config: Configuration):
    global ids
    if len(ids) == 0:
        js = __fetch_next_data(__listing_url(list(config.areas)))

        rows_found = js["props"]["pageProps"]["searchResult"]['rowsFound']
        rows_returned = js["props"]["pageProps"]["searchResult"]['rowsReturned']

        print(rows_found)

        ret = []
        for page in range(2, math.ceil(rows_found / rows_returned) + 2):
            rows_found = js["props"]["pageProps"]["searchResult"]['rowsFound']
            rows_returned = js["props"]["pageProps"]["searchResult"]['rowsReturned']

            print(f"{rows_returned}/{rows_found}")

            results = js["props"]["pageProps"]["searchResult"]['advertSummaryList']['advertSummary']
            ret += [r['id'] for r in results]
            js = __fetch_next_data(__listing_url(list(config.areas), page))

        ids = set(ret)
    return ids


raw_listings = {}
def fetch_raw_listing(id):
    global raw_listings

    if id not in raw_listings:
        url = f'https://www.willhaben.at/webapi/iad/atverz/{id}?formatEnableHtmlTags=true'
        raw_listings[id] = json.loads(session.get(url).text)

    return raw_listings[id]


def fetch_details(id):
    data = fetch_raw_listing(id)
    attr_list = data['attributes']['attribute']
    ret = {}

    for d in attr_list:
        if d['name'] not in ret:
            ret[d['name']] = []

        ret[d['name']] += d['values']

    ret['raw'] = data

    return ret


def fetch_attributes(id):
    data = fetch_raw_listing(id)
    attrs = data['attributeInformation']

    return dict([(a['treeAttributeElement']['code'], a['treeAttributeElement']['label']) for a in attrs])

