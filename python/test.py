import pkgutil

from python import database
from python.data.configuration import build_configuration

config = build_configuration()
_db = database.db(config)

# 'ADDITIONAL_COST/FEE'

listing = _db.listings["580116899"]

print([name for _, name, _ in pkgutil.iter_modules(['patches'])])