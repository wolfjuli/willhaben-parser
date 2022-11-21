from python import database, scoring
from python.types.configuration import build_configuration

config = build_configuration()
db = database.db(config)

# 'ADDITIONAL_COST/FEE'

listing = db.listings["580116899"]

print(scoring.score(listing).calculated_scores)
print(scoring.score(listing).calculated_prices)