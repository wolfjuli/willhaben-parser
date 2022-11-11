class Listing:
    def __int__(self, _id=-1, attributes={}):
        self.id = _id
        self.attributes = attributes


class Rating:
    def __int__(self, listing_id=-1, calculated_rating=-1, user_rating=-1):
        self.listing_id = listing_id
        self.calculated_rating = calculated_rating
        self.user_rating = user_rating
