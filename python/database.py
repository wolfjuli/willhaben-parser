import os.path
import pickle

from configuration import Configuration
from python.data.structs import Listing, AttributeDef


class Database:
    def __init__(self, db_path):
        self.listings = {}
        self.ratings = {}
        self.attributes = {}

        self.__dirty_counter = 0
        self.db_path = db_path

    def listing_ids(self):
        return list(self.listings)

    def create_listing(self, _id):
        if _id not in self.listings:
            self.listings[_id] = Listing(_id)
            self.__dirty()

        return self.listings[_id]

    def update_listing(self, _id, attributes=None):
        listing = self.create_listing(_id)
        listing.attributes = attributes if attributes else listing.attributes

        self.__dirty()
        return listing

    def update_attribute(self, _id, name, force=False):
        if _id in self.attributes:
            attr = self.attributes[_id]

            if not force:
                return attr

            if attr.name != name:
                print(f"Attribute {_id}: changing value from '{attr.name}' to '{name}'")
        else:
            attr = AttributeDef(_id, name)

        attr.name = name
        self.attributes[_id] = attr
        self.__dirty()

        return attr

    def __dirty(self):
        self.__dirty_counter += 1

        if self.__dirty_counter >= 100:
            self.flush()

    def flush(self):
        if not self.db_path:
            raise Exception("There is no database path defined")

        os.makedirs(os.path.dirname(self.db_path), exist_ok=True)
        with open(self.db_path, 'wb') as f:
            pickle.dump(self, f)

        self.__dirty_counter = 0

    def reload(self):
        if os.path.exists(self.db_path):
            with open(self.db_path, 'rb') as f:
                data = pickle.load(f)

            self.listings = data.listings
            self.ratings = data.ratings
            self.attributes = data.attributes


__data = Database("")


def db(config: Configuration = None):
    global __data

    if not __data.db_path:
        if not config:
            raise Exception("DB needs to be initialized with a config")

        if os.path.exists(config.database_path):
            with open(config.database_path, 'rb') as f:
                __data = pickle.load(f)

            __data.db_path = config.database_path
        else:
            __data = Database(config.database_path)
            __data.flush()

    return __data



