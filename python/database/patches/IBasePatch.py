from python.database.IBaseDatabase import IBaseDatabase


class IBasePatch:
    def run(self, db: IBaseDatabase):
        """Run the patch on the given database"""
        pass