import os.path
import pickle

from python.defintions import configuration_file, database_file, area_ids

__file = configuration_file
__instance = None


class Configuration:
    def __init__(self, db_path=database_file, areas=list(area_ids)):
        self.database_path = db_path
        self.areas = areas


def build_configuration():
    global __instance

    if not __instance:
        if os.path.exists(__file):
            with open(__file, 'rb') as f:
                __instance = pickle.load(f)
        else:
            __instance = Configuration()
            flush()

    return __instance


def flush():
    global __file
    global __instance

    os.makedirs(os.path.dirname(__file), exist_ok=True)
    with open(__file, 'wb') as f:
        pickle.dump(__instance, f)
