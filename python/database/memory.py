import os.path
import pickle
import pkgutil

from python.data.configuration import Configuration
from python.data import Listing, AttributeDef, Distance
from python.database.IBaseDatabase import IBaseDatabase


class Memory(IBaseDatabase):
    def __init__(self, db_path):
        self.listings = {}
        self.scores = {}
        self.attributes = {}
        self.distances = {}
        self.schema_version = {}

        self.__dirty_counter = 0
        self.db_path = db_path

    def listing_ids(self):
        return list(self.listings)

    def listing(self, _id):
        return self.listings[_id] if _id in self.listings else None

    def listing_attribute_values(self, attribute):
        ret = set()
        for listing in self.listings.values():
            if attribute in listing.attributes:
                for val in listing.attributes[attribute]:
                    ret.add(val)

        return ret

    def create_listing(self, _id):
        if _id not in self.listings:
            self.listings[_id] = Listing(_id)
            self.__dirty()

        return self.listings[_id]

    def update_listing(self, _id, attributes=None):
        listing = self.create_listing(_id)
        listing.attributes = attributes if attributes else listing.attributes
        listing.attributes = dict([
            (
                key,
                listing.attributes[key].__dict__ if hasattr(listing.attributes[key], '__dict__') else
                listing.attributes[key]
            ) for key in listing.attributes
        ])

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

    def update_distance(self, distance: Distance):
        if not hasattr(self, 'distances'):
            self.distances = {}

        if distance.frm not in self.distances:
            self.distances[distance.frm] = []

        self.distances[distance.frm] += [distance]
        self.__dirty()

        return distance

    def update_score(self, score):
        if score.listing_id not in self.listings:
            raise Exception(f"Score references non existing listing id {score.id}")

        self.scores[score.listing_id] = score
        self.__dirty()
        return score

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
            self.scores = data.scores
            self.attributes = data.attributes

    def upgrade(self):
        patches = [name for _, name, _ in pkgutil.iter_modules(['testpkg'])]


def db(config: Configuration = None):
    data = Memory("")

    if not data.db_path:
        if not config:
            raise Exception("DB needs to be initialized with a config")

        if os.path.exists(config.database_path):
            with open(config.database_path, 'rb') as f:
                data = pickle.load(f)

            data.db_path = config.database_path
            data.upgrade()

        else:
            data = Memory(config.database_path)
            data.flush()

    return data
