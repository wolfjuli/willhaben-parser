import cgi
import re
import time
from http.server import BaseHTTPRequestHandler, HTTPServer

import json

from python.data.configuration import build_configuration
from python.database import db

config = build_configuration()
db = db(config)

last_refresh = time.time()


def listing_overviews():
    return sorted([{
        "id": score.listing_id,
        "name": db.listing(score.listing_id).attributes['raw']['description'],
        "calculatedScores": score.calculated_scores,
        "userScores": score.user_scores,
        "calculatedPrices": score.calculated_prices,
    } for score in db.scores.values()],
        key=lambda it: sum(it['userScores'].values()) * 1000 + sum(it['calculatedScores'].values()), reverse=True)


routes = {
    '/listings/{id}': lambda params: db.listing(params['id']).__dict__,
    '/listings': lambda _: [a.__dict__ for a in db.listings.values()],
    '/attributes': lambda _: [a.__dict__ for a in db.attributes.values()],
    '/listing-overviews': lambda _: listing_overviews()
}
frontend_path = "frontend/public"


class Server(BaseHTTPRequestHandler):
    def _set_json_headers(self):
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()

    def _set_html_headers(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.send_header('encoding', 'utf-8')
        self.end_headers()

    def _set_fail_headers(self):
        self.send_response(207)
        self.send_header('Content-type', 'application/json')
        self.end_headers()

    def do_HEAD(self):
        self._set_json_headers()

    def __refresh_db(self):
        global last_refresh

        now = time.time()
        if last_refresh - now > 5:
            db.reload()
            last_refresh = now

    param_regex = re.compile("\{([^}]+)\}")

    def __regex_path(self, path):
        return re.sub(self.param_regex, "(?P<\\1>[^/])", path)

    def do_GET(self):
        self.__refresh_db()
        if self.path.startswith("/api"):
            path = self.path[4:]
            self._set_json_headers()

            for target_path in routes:
                regex = self.__regex_path(target_path)
                match = re.match(regex, path)
                if match:
                    try:
                        j = json.dumps({'data': routes[path](match.groupdict())})
                        self.wfile.write(bytes(j, encoding="utf-8"))
                    except Exception as e:
                        print(f"Path: {path}")
                        raise e
                    return

            self.wfile.write(bytes(json.dumps({'error': f'Unknown Path: {path}'}), encoding="utf-8"))

        else:
            self._set_html_headers()
            if self.path == "/":
                path = "/index.html"
            else:
                path = self.path

            with open(frontend_path + path, 'rb') as f:
                self.wfile.write(f.read())

    # POST echoes the message adding a JSON field
    def do_POST(self):
        ctype, pdict = cgi.parse_header(self.headers.getheader('content-type'))

        # refuse to receive non-json content
        if ctype != 'application/json':
            self.send_response(400)
            self.end_headers()
            return

        # read the message and convert it into a python dictionary
        length = int(self.headers.getheader('content-length'))
        message = json.loads(self.rfile.read(length))

        # add a property to the object, just to mess with data
        message['received'] = 'ok'

        # send the message back
        self._set_json_headers()
        self.wfile.write(json.dumps(message))


def run(server_class=HTTPServer, handler_class=Server, port=2468):
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)

    print('Starting httpd on port %d...' % port)
    httpd.serve_forever()


if __name__ == "__main__":
    from sys import argv

    if len(argv) == 2:
        run(port=int(argv[1]))
    else:
        run()
