import os.path
import pickle
import json

from python.defintions import configuration_file, database_file, area_ids

__file = configuration_file
__instance = None


class Configuration:
    def __init__(self, db_path=database_file, areas=list(area_ids)):
        self.database_path = db_path
        self.areas = areas

    def from_dict(self, dict):
        self.__dict__.update(dict)


def build_configuration():
    global __instance

    __instance = Configuration()

    if not __instance:
        if os.path.exists(__file):
            with open(__file, 'r') as f:
                try:
                    conf_dict = json.load(f)
                    __instance.from_dict(conf_dict)
                except:
                    flush()
        else:
            flush()

    return __instance


def flush():
    global __file
    global __instance

    os.makedirs(os.path.dirname(__file), exist_ok=True)
    with open(__file, 'w') as f:
        json.dump(__instance.__dict__, f)
