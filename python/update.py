# This is a sample Python script.
import math

import database
import willhaben
from configuration import build_configuration


def add_new_listings(db, for_ids):
    if len(for_ids) > 0:
        print(f"Creating {len(for_ids)} new listings")
        for idx, id in enumerate(for_ids):
            print(f"{id} - {math.floor((idx + 1) / len(for_ids) * 100)} %")
            attr = willhaben.fetch_details(id)

            db.update_listing(id, attr)

            for attr_id in attr:
                db.update_attribute(attr_id, attr_id)

        db.flush()


def add_new_attributes(db, for_ids):
    if len(for_ids) > 0:
        print(f"Checking {len(for_ids)} for attributes")
        for idx, id in enumerate(for_ids):
            print(f"{id} - {math.floor((idx + 1) / len(for_ids) * 100)}%")
            attrs = willhaben.fetch_attributes(id)

            for attr_id in attrs:
                value = attrs[attr_id]
                db.update_attribute(attr_id, value, force=True)

        db.flush()


if __name__ == '__main__':
    config = build_configuration()
    db = database.db(config)
    known = db.listing_ids()
    ids = willhaben.get_ids(config)
    unknown = [id for id in ids if id not in known]
    #unknown = known

    add_new_listings(db, unknown)
    add_new_attributes(db, unknown)





# See PyCharm help at https://www.jetbrains.com/help/pycharm/
