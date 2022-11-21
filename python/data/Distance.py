from python.data import LatLong


class Distance:
    def __init__(self, frm: LatLong, to: LatLong, distance: int = 0):
        self.frm = frm
        self.to = to
        self.distance = distance

    def dict(self):
        return {
            "frm": self.frm.__dict__,
            "to": self.to.__dict__,
            "distance": self.distance
        }
