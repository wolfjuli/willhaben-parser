
class Score:
    def __init__(self, listing_id=-1, calculated_scores={}, calculated_prices={}, user_scores={}):
        self.listing_id = listing_id
        self.calculated_scores = calculated_scores
        self.user_scores = user_scores
        self.calculated_prices = calculated_prices
