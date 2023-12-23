import busio

from adafruit_bme680 import Adafruit_BME680_I2C

def measure(scl, sda):
    with busio.I2C(scl, sda) as i2c:
        bme680 = Adafruit_BME680_I2C(i2c, address= 0x76)
        bme680.sea_level_pressure = 1013.25

        if (bme680.relative_humidity == 100) {
            raise Exception('BME680 not ready yet: high humidity')
        }
        
        while True:
            yield {
                'temperature_mc': round(bme680.temperature * 10),
                'humidity_pt': round(bme680.relative_humidity * 10)
            }
            