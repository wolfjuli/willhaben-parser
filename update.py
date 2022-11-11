# This is a sample Python script.
import lib
from configuration import ConfigurationBuilder

if __name__ == '__main__':
    config = ConfigurationBuilder().build()
    db = Database(config)

    #lib.test2()

# See PyCharm help at https://www.jetbrains.com/help/pycharm/
