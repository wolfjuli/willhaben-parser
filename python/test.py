from python.data.configuration import build_configuration
from python.database.sqlite import db

config = build_configuration()
_db = db(config)

_db.upgrade()
