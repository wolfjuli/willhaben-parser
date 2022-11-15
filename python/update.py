# This is a sample Python script.
import math

import database
import willhaben
from configuration import build_configuration
from python import mapbox
from python.data.structs import LatLong
from python.scoring import score, distances_to


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


def add_distances(db, for_ids):
    if len(for_ids) > 0:
        print(f"Calculate score for {len(for_ids)} listings")

        for idx, id in enumerate(for_ids):
            listing = db.listing(id)
            coords = listing.string_attribute('COORDINATES')
            if coords:
                (lat, long) = coords.split(",")
                frm = LatLong(lat, long)
                dists = mapbox.distance(frm, distances_to)

                for d in dists:
                    db.update_distance(d)

                listing.attributes['DISTANCES'] = list(map(lambda d: d.__dict__, db.distances[frm]))
                db.update_attribute(listing.id, listing.attributes)

            print(f"{id} - {math.floor((idx + 1) / len(for_ids) * 100)}%")

        db.flush()


def add_new_scores(db, for_ids):
    if len(for_ids) > 0:
        print(f"Calculate score for {len(for_ids)} listings")

        for idx, id in enumerate(for_ids):
            listing = db.listing(id)
            s = db.update_score(score(listing))

            calc_score = sum(s.calculated_scores.values())
            print(f"{id}: {calc_score} - {math.floor((idx + 1) / len(for_ids) * 100)}%")

        db.flush()


def add_new_listings(db):
    known = db.listing_ids()
    ids = willhaben.get_ids(config)
    unknown = [id for id in ids if id not in known]
    # unknown = known

    add_new_listings(db, unknown)
    add_new_attributes(db, unknown)
    add_distances(db, unknown)
    add_new_scores(db, unknown)


def recalculate_scores(db):
    known = db.listing_ids()
    add_new_scores(db, known)


def update_attributes(db: database.Database):
    for listing in db.listings.values():
        if 'distances' in listing.attributes:
            listing.attributes['DISTANCES'] = list(map(lambda d: d.dict(), listing.list_attribute('distances')))
            listing.attributes.pop('distances')

            db.update_listing(listing.id, listing.attributes)

    db.flush()


if __name__ == '__main__':
    config = build_configuration()
    db = database.db(config)

    #recalculate_scores(db)
    update_attributes(db)

# See PyCharm help at https://www.jetbrains.com/help/pycharm/
