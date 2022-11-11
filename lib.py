import json

import requests
from bs4 import BeautifulSoup

area_ids = {
    601: "Graz",
    603: "Deutschlandsberg",
    606: "Graz-Umgebung",
    610: "Leibnitz",
    616: "Voitsberg"
}

headers = {'authority': 'www.willhaben.at',
           'accept': 'application/json',
           'accept-language': 'en-US,en;q=0.9',
           'cookie': 'IADVISITOR=4d3cb342-ece0-4b15-b35c-dff894248b0e; context=prod; TRACKINGID=6486a898-491a-40fa-99c1-7df7e1b6d470; x-bbx-csrf-token=d972c2aa-5e9d-4c42-b604-f6c25e21e325; RANDOM_USER_GROUP_COOKIE_NAME=21; SRV=3|Y25kC',
           'referer': 'https://www.willhaben.at/iad/immobilien/haus-kaufen/haus-angebote?sfId=528fed80-90c1-4432-92bb-1f14fd8019d3&isNavigation=true&rows=90&areaId=601&areaId=603&areaId=606&areaId=610&areaId=616&page=1',
           'sec-ch-ua': '"Not-A.Brand";v="99", "Opera GX";v="91", "Chromium";v="105"',
           'sec-ch-ua-mobile': '?0',
           'sec-ch-ua-model': '',
           'sec-ch-ua-platform': '"macOS"',
           'sec-ch-ua-platform-version': '"12.6.0"',
           'sec-fetch-dest': 'empty',
           'sec-fetch-mode': 'cors',
           'sec-fetch-site': 'same-origin',
           'user-agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36 OPR/91.0.4516.106 (Edition std-1)',
           'x-bbx-csrf-token': 'd972c2aa-5e9d-4c42-b604-f6c25e21e325',
           'x-wh-client': 'api@willhaben.at;responsive_web;server;1.0.0;desktop'
           }
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
