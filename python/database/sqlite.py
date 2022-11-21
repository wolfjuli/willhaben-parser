import os.path
import sqlite3

from python.data.configuration import Configuration
from python.data import Listing, AttributeDef, Distance
from python.database.IBaseDatabase import IBaseDatabase
from python.database.patches import IBasePatch


class SQLite(IBaseDatabase):
    def __init__(self, db_path):
        self.db_path = db_path
        self.__db = sqlite3.connect(db_path, detect_types=True)

    def cursor(self):
        return self.__db.cursor()

    def listing_ids(self):
        for a in self.__db.execute("SELECT * from listings").fetchall():
            pass

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
        pass

    def reload(self):
        if os.path.exists(self.db_path):
            with open(self.db_path, 'rb') as f:
                data = pickle.load(f)

            self.listings = data.listings
            self.scores = data.scores
            self.attributes = data.attributes

    def upgrade(self):
        try:
            cursor = self.__db.execute("SELECT max(id) as id FROM schema_versions").fetchall()
            max = cursor.pop()[0]
        except Exception as e:
            max = -1

        all_patches = [patch for patch in self.__all_patches("sqlite") if patch.number > max]

        if len(all_patches) > 0:
            module = __import__("python.database.patches.sqlite")
            for patch in sorted(all_patches):
                class_ = getattr(module, patch.file_name)
                instance: IBasePatch = class_()
                instance.run(self)






def db(config: Configuration = None):
    if not config:
        raise Exception("DB needs to be initialized with a config")

    data = SQLite(config.database_path)
    data.upgrade()
    data.flush()

    return data
