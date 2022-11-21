from python.server import db
from python.data.configuration import build_configuration


def init_database():
    config = build_configuration()
    return db(config)
