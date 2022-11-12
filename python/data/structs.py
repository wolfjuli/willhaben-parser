class Listing:
    def __init__(self, _id=-1, attributes={}):
        self.id = _id
        self.attributes = attributes

class UserListing:
    def __init__(self, _id=-1, attributes={}):
        self.id = _id
        self.attributes = attributes


class Rating:
    def __init__(self, listing_id=-1, calculated_rating=-1, user_rating=-1):
        self.listing_id = listing_id
        self.calculated_rating = calculated_rating
        self.user_rating = user_rating


class AttributeDef:
    def __init__(self, id, name=id):
        self.id = id
        self.name = name
