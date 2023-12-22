import board
import busio
import time

import settings

ADDRESS = 0x27
    
def convert(result):
    humidity = 0b0
    temperature = 0b0
    
    byte1 = result[0]
    status = byte1 >> 6
    humidity |= byte1 & 63
    humidity = humidity << 8
    
    byte2 = result[1]
    humidity |= byte2
    
    byte3 = result[2]
    temperature = byte3
    temperature = temperature << 6
    
    byte4 = result[3]
    temperature |= byte4 >> 2
    
    humidity = 100 * humidity / ((2<<13) - 2)
    temperature = (165 * temperature / ((2<<13) - 2)) - 40

    return {
        'temperature_mc': round(temperature * 10),
        'humidity': round(humidity),
    }


def measure(scl=settings.HIH8000_SCL, sda=settings.HIH8000_SDA):
    with busio.I2C(scl, sda) as i2c:
        while not i2c.try_lock():
            pass

        i2c.writeto(ADDRESS, bytes())
        time.sleep(0.200)
        result = bytearray(4)
        i2c.readfrom_into(ADDRESS, result)
        return convert(result)
        

