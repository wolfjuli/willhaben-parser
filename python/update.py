# This is a sample Python script.
import math

import database
import willhaben
from configuration import build_configuration

if __name__ == '__main__':
    config = build_configuration()
    db = database.db(config)

    known = db.listing_ids()
    ids = willhaben.get_ids(config)

    unknown = [id for id in ids if id not in known]

    if len(unknown) > 0:
        print(f"Creating {len(unknown)} new listings")
        for idx, id in enumerate(unknown):
            print(f"{id} - {math.floor((idx + 1) / len(unknown) * 100)} %")
            attr = willhaben.fetch_details(id)

            db.update_listing_attributes(id, attr)

        db.flush()


# See PyCharm help at https://www.jetbrains.com/help/pycharm/
