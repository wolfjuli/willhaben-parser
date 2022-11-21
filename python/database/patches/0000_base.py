from python.server import db
from python.types import configuration
from python.types.configuration import build_configuration


def init_database():
    config = build_configuration()
    return db(config)
