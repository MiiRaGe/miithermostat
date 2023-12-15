#!/usr/bin/env python3

import sys
import requests
import time
from urllib3.exceptions import NewConnectionError
from requests.exceptions import ConnectionError

API_BASE_URL = 'http://localhost/api'

class fake_adafruit_requests:
    def Session(*args, **kwargs):
        return requests

class fake_hih8000:
    temperature = 10
    humidity = 10
    
    def measure(self):
        self.temperature = (self.temperature + 1) % 50
        self.humidity = (self.humidity + 1) % 40 + 10
        return {
            'temperature_mc': round(self.temperature * 10),
            'humidity': round(self.humidity),
        }

class fake_microcontroller:
    cpu = {}

class fake_socketpool:
    def SocketPool(*args, **kwargs):
        pass

class wlan:
    ssid = ''
    def __init__(self, ssid):
        self.ssid = ssid

class radio:
    connected = False
    
    def start_scanning_networks(self):
        return [wlan("WIFI1"), wlan("fake_ssid")]
    
    def connect(self, *args, **kwargs):
        self.connected = True
    
class wifi:
    radio = radio()

class local_secrets:
    WIFI_SSID = 'wlan'
    WIFI_PASSWORD = 'password'
    API_URL = 'url'
    DEVICE_ID = 'id'
    SECRETS = {
        WIFI_PASSWORD: 'fake_password',
        WIFI_SSID: 'fake_ssid',
        DEVICE_ID: 'a8cf',
        API_URL: '{}/measurements'.format(API_BASE_URL),
    }
    def get_device_id():
        return "FAKE_DEVICE"
    
class onboard_led:
    def blink(*args):
        print('--- BLINKING ONBOARD LED ---')
        
    def set_error_led(value = True):
        print('--- SET ERROR: {}'.format(value))

sys.modules["hih8000"] = fake_hih8000()
sys.modules["microcontroller"] = fake_microcontroller
sys.modules["adafruit_requests"] = fake_adafruit_requests
sys.modules["socketpool"] = fake_socketpool
sys.modules["wifi"] = wifi
sys.modules["local_secrets"] = local_secrets
sys.modules["onboard_led"] = onboard_led

from lib import server_api

sys.modules["server_api"] = server_api

from main import start_device

def with_exponential_retry(func):
    def wrapped_func(*args, **kwargs):
        count = 1
        status = None
        while True:
            try:
                response = func(*args, **kwargs)
                if (response.status_code == 502):
                    raise ConnectionError('Server not up yet')
                break
            except (ConnectionError, NewConnectionError) as e:
                print('--- Server not up yet, waiting {} seconds ---'.format(count))
                time.sleep(count)
                count *= 2
    return wrapped_func

@with_exponential_retry
def register_device(device_id):
    return requests.post('{}/devices/'.format(API_BASE_URL), json={'id': device_id})

@with_exponential_retry
def register_location(location_name):
    return requests.post('{}/locations/'.format(API_BASE_URL), json={'name': location_name})
 
@with_exponential_retry   
def register_device_and_location(device_id, location_name):
    register_device(device_id)
    register_location(location_name)
    return requests.post('{}/locations/{}/devices/'.format(API_BASE_URL, location_name), json={'id': device_id})

if __name__ == "__main__":
    device_id = sys.argv[1]
    if (len(sys.argv) > 2):
        register_device_and_location(device_id, sys.argv[2])
    else:
        register_device(device_id)
    start_device(device_id)