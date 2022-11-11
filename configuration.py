import os.path
import pickle

from defintions import configuration_file, database_file, area_ids


class ConfigurationBuilder:
    file = configuration_file

    def build(self):
        if os.path.exists(self.file):
            with open(self.file, 'rb') as f:
                return pickle.load(f)
        else:
            c = Configuration()
            with open(self.file, 'wb') as f:
                pickle.dump(c, f)

            return c


class Configuration:
    def __init__(self, db_path=database_file, areas=list(area_ids)):
        self.database_path = db_path
        self.areas = areas
