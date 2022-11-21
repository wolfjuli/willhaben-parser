class LatLong:
    def __init__(self, lat, long, name=""):
        self.lat = lat
        self.long = long
        self.name = name

    def reverse_string(self):
        return f"{self.long},{self.lat}"

    def __str__(self):
        return f"{self.lat},{self.long}"

