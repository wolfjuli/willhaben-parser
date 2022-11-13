class Listing:
    def __init__(self, _id=-1, attributes={}):
        self.id = _id
        self.attributes = attributes

    def list_attribute(self, attribute):
        return self.attributes[attribute] if attribute in self.attributes else []

    def string_attribute(self, attribute):
        r = self.list_attribute(attribute)
        return r[0] if len(r) > 0 else None

    def float_attribute(self, attribute):
        r = self.string_attribute(attribute)
        try:
            return float(r) if r else 0
        except ValueError:
            return 0


class UserListing(Listing):
    pass


class Score:
    def __init__(self, listing_id=-1, calculated_score=-1, calculated_price=-1, user_score=-1):
        self.listing_id = listing_id
        self.calculated_score = calculated_score
        self.user_score = user_score
        self.calculated_price = calculated_price


class AttributeDef:
    def __init__(self, id, name=id):
        self.id = id
        self.name = name


class LatLong:
    def __init__(self, lat, long, name=""):
        self.lat = lat
        self.long = long
        self.name = name

    def reverse_string(self):
        return f"{self.long},{self.lat}"

    def __str__(self):
        return f"{self.lat},{self.long}"


class Distance:
    def __init__(self, frm: LatLong, to: LatLong, distance: int = 0):
        self.frm = frm
        self.to = to
        self.distance = distance
