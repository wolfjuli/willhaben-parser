import pkgutil

from python.data.Distance import Distance
from python.data.Patch import Patch


class IBaseDatabase:

    def listing_ids(self):
        """Fetch all listings"""
        pass

    def listing(self, _id):
        """Fetch one listing"""
        pass

    def listing_attribute_values(self, attribute):
        """Fetch all possible values of the given attribute (over all listings)"""
        pass

    def create_listing(self, _id):
        """Create a new listing"""
        pass

    def update_listing(self, _id, attributes=None):
        """Update/Add attribute(s) to listing"""
        pass

    def update_attribute(self, _id, name, force=False):
        """Update/Add metadata to attribute"""
        pass

    def update_distance(self, distance: Distance):
        """Update/Add distance"""

    def update_score(self, score):
        """Update/Add score of listing (listing_id must exist)"""

    def flush(self):
        """Flush to disk (if necessary)"""

    def reload(self):
        """Update in-memory data with file content (if necessary)"""

    def upgrade(self):
        """Upgrade schema to a newer version"""

    def __all_patches(self, package):
        name_parts = [[name] + name.split("_") for _, name, _ in pkgutil.iter_modules([f'database/patches/{package}'])]
        return [Patch(int(parts[1]), "_".join(parts[2:]), parts[0]) for parts in name_parts]
