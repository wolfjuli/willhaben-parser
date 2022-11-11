import os.path
import pickle

from configuration import Configuration
from data.structs import Listing


class Data:
    def __int__(self):
        self.listings = {}
        self.ratings = {}


class Database:
    def __init__(self, config: Configuration):
        self.dirty_counter = 0

        if os.path.exists(config.database_path):
            with open(config.database_path, 'rb') as f:
                self.data = pickle.load(f)
        else:
            with open(config.database_path, 'wb') as f:
                self.data = Data()
                pickle.dump(self.data, f)

    def listing_ids(self):
        return list(self.data.listings)

    def create_listing(self, id):
        if id not in self.data.listings:
            self.dirty_counter += 1
            self.data.listings[id] = Listing(id)

        return self.data.listings[id]

    def update_attributes(self, _id, attributes={}):
        listing = self.create_listing(_id)
        listing.attributes = attributes

        self.dirty_counter += 1
        return listing

