import time


class SchemaVersion:
    def __init__(self):
        self.version = -1
        self.description = ""
        self.timestamp = time.time()
