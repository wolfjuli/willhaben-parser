from python import database
from python.configuration import build_configuration

config = build_configuration()
db = database.db(config)

# 'ADDITIONAL_COST/FEE'

for listing in db.listings.values():
    print(listing)
