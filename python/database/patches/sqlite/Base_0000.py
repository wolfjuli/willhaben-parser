import sqlite3

from python.database.patches.IBasePatch import IBasePatch
from python.database.sqlite import SQLite


class Base(IBasePatch):
    def run(self, db: SQLite):
        print("running Patch 0")
        c = db.cursor()
        sql = ";\n".join([
            self.schema_versions,
            self.listings,
            self.distances,
            self.attribute_keys,
            self.attributes,
            self.listing_distances
        ])

        try:
            c.executescript(f"""
            BEGIN;
                {sql};

                INSERT INTO schema_versions(id, name) values (0, 'Init');
            COMMIT;
            """)
        except sqlite3.Error as e:
            print("error during schema creation", e)
            c.rollback()

    def __init__(self):
        self.schema_versions = """
        CREATE TABLE schema_versions(
            id INT NOT NULL PRIMARY KEY ,
            name TEXT,
            created timestamp DEFAULT current_timestamp
        )"""
        self.listings = """
        CREATE TABLE listings(
            id int primary key autoincrement ,
            name text,
            description text
        );
        """
        self.attribute_keys = """
        CREATE TABLE attribute_keys(
            id int primary key autoincrement ,
            name text not null UNIQUE,
            translated text
        )
        """
        self.distances = """
        CREATE TABLE distances(
        id int primary key autoincrement ,
        from_lat real not null,
        from_long real not null,
        from_address text,
        to_lat real not null,
        to_long real not null,
        to_address text,
        distance int not null
        )
        """
        self.attributes = """
        CREATE TABLE attributes (
        listing_id int not null references listings(id),
        attribute_key_id int not null references attribute_keys(id),
        value text
        )
        """
        self.listing_distances = """
        CREATE TABLE listing_distances(
            listing_id int not null references listings(id),
            distance_id int not null references distances(id)
        )
        """
