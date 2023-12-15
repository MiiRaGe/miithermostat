import digitalio
import time

import adafruit_dht

from settings import RHT03_POWER, RHT03_DATA


def measure(data_pin=RHT03_DATA, power_pin=RHT03_POWER):
    power_dio = digitalio.DigitalInOut(power_pin)
    power_dio.switch_to_output(True)
    time.sleep(1)
    # Let the sensor boot up after being powered

    try:
        dht_device = adafruit_dht.DHT22(data_pin)
        time.sleep(1)
        dht_device.measure()

        time.sleep(0.5)  # Give some time for the sensor to read

        data = {
            'temperature_mc': round(dht_device.temperature * 10),
            'humidity': round(dht_device.humidity),
        }

        power_dio.value = False
        power_dio.deinit()
        dht_device.exit()
        return data
    except Exception as e:
        power_dio.deinit()
        dht_device.exit()
        raise e

