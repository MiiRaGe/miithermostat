from microcontroller import cpu

import adafruit_hashlib as hashlib

WIFI_SSID = 'wifi_ssid'
WIFI_PASSWORD = 'wifi_password'
API_URL = 'api_url'
DEVICE_ID = 'device_id'

def get_device_id():
    m = hashlib.sha256()
    m.update(cpu.uid)
    return m.hexdigest()[-4:]

SECRETS = {
    WIFI_SSID:'home_wifi', # Wifi ssid
    WIFI_PASSWORD: 'good password', # Wifi password
    API_URL: 'http://127.0.0.1:8080/api', # Where to post measurements
    DEVICE_ID: get_device_id(), # e.g.last 5 char of sha1 digest of CPU UID
}