import json

import requests

from bs4 import BeautifulSoup
from defintions import headers

session = requests.Session()
session.headers.update(headers)


def to_object(result):
    links = result["contextLinkList"]['contextLink']
    main_link = next(filter(lambda l: l["id"] == "iadShareLink", links))["uri"]

    return {
        "id": result['id'],
        "link": main_link
    }


def test():
    # areaId=???
    # max 90 results: rows=90
    # page=1
    #
    url = "https://www.willhaben.at/iad/immobilien/haus-kaufen/haus-angebote?sfId=528fed80-90c1-4432-92bb-1f14fd8019d3&isNavigation=true&rows=90&areaId=601&areaId=603&areaId=606&areaId=610&areaId=616&page=1"
    res = session.get(url)
    session.get("https://www.willhaben.at/webapi/iad/vertical")
    session.get("https://www.willhaben.at/webapi/iad/user/me")
    session.get("https://www.willhaben.at/nodeapi/toggles")

    soup = BeautifulSoup(res.text, "html.parser")
    js = json.loads(soup.find("script", {"id": '__NEXT_DATA__'}).text)

    rows_found = js["props"]["pageProps"]["searchResult"]['rowsFound']
    rows_returned = js["props"]["pageProps"]["searchResult"]['rowsReturned']
    results = js["props"]["pageProps"]["searchResult"]['advertSummaryList']['advertSummary']

    result = results[0]
    as_obj = to_object(result)
    print(fetch_details(as_obj['id']))

    return



def fetch_details(id):
    url = f'https://www.willhaben.at/webapi/iad/atverz/{id}?formatEnableHtmlTags=true'
    print(url)
    raw_result = session.get(url)
    print(raw_result)
    data = json.loads(session.get(url).text)
    return data['attributes']['attribute']
