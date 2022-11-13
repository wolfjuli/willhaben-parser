import math

from python import mapbox
from python.data.structs import Score, LatLong
from python.mapbox import graz_hauptplatz, soeding

target_price = 450000
estate_size = 140
plot_area = 1200

dealer_price_add = 1.036


condition = {
    "Sehr gut/gut": 5,
    "Neuwertig": 10,
    "Sanierungsbedürftig": -5,
}

condition_price_change = {
    "Sanierungsbedürftig": 100000,
}

estate_preference = {
    "Garage": 10,
    "Fahrstuhl": 10,
    "Carport": 5,
}

heating_price_change = {
    'Ölheizung': 15000,
    'Gasheizung': 15000,
}

heating_score = {
    'Solar': 5,
    'Fernwärme': 5,
    'Erdwärme': 5,
    'Fußbodenheizung': 5,
    'Luftwärmepumpe': 5,
    'Elektroheizung': -5,
    'Ölheizung': -5,
    'Gasheizung': -5,
    'Pellets': -3,
    'Biomasse': -3,
}

property_type_score = {
    'Doppelhaushälfte': -100,
    'Doppelhaus': -100,
    'Reihenhaus': -100,
    'Reihenmittelhaus': -100,
    'Mehrfamilienhaus': -10,
    'Mehrfamilien': -10
}

distance_score = 10
distances_to = {
    graz_hauptplatz.name: 10000,
    soeding.name: 10000
}

def score(listing):
    calculated_price = listing.float_attribute('PRICE')
    if listing.float_attribute('DEALER'):
        calculated_price *= dealer_price_add
    calc_score = 0

    calc_score += math.atan(listing.float_attribute('PLOT/AREA') - plot_area)
    calc_score += math.atan(listing.float_attribute('ESTATE_SIZE/USABLE_AREA') - estate_size)
    calc_score += condition[listing.string_attribute('BUILDING_CONDITION')] \
        if listing.string_attribute('BUILDING_CONDITION') in condition else 0
    calculated_price += condition_price_change[listing.string_attribute('BUILDING_CONDITION')] \
        if listing.string_attribute('BUILDING_CONDITION') in condition_price_change else 0
    for a in listing.list_attribute('ESTATE_PREFERENCE'):
        if a in estate_preference:
            calc_score += estate_preference[a]

    heating = listing.string_attribute('HEATING')
    if heating in heating_score:
        calc_score += heating_score[heating]

    if heating in heating_price_change:
        calculated_price += heating_price_change[heating]

    property_type = listing.string_attribute('PROPERTY_TYPE')
    if property_type in property_type_score:
        calc_score += property_type_score[property_type]

    for key in property_type_score:
        if key in listing.attributes['raw']['description']:
            calc_score += property_type_score[key]

    distances = listing.list_attribute('distances') or []
    for d in distances:
        if d.to.name in distances_to:
            calc_score += math.atan(distances_to[d.to.name] - d.distance) * distance_score

    calc_score += math.atan(target_price - calculated_price) * 10

    return Score(
        listing.id,
        calculated_score=calc_score,
        calculated_price=calculated_price
    )
