
class Listing:
    def __init__(self, _id=-1, name = "", description = ""):
        self.id = _id
        self.attributes = {}
        self.distances = []
        self.name = name
        self.descriptionn = description
        self.prices = []

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

