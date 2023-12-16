import adafruit_requests
import socketpool
import wifi

from time import sleep

from local_secrets import SECRETS, WIFI_PASSWORD, WIFI_SSID, API_URL, DEVICE_ID


wifi_ssid = SECRETS[WIFI_SSID]
wifi_password = SECRETS[WIFI_PASSWORD]
api_base_url = SECRETS[API_URL]

session = adafruit_requests.Session(socketpool.SocketPool(wifi.radio))


def connect():
    count = 1
    while wifi.radio.connected != True:
        wlans = wifi.radio.start_scanning_networks()
        ssid_list = [x.ssid for x in wlans]
        if wifi_ssid in ssid_list:
            try:
                wifi.radio.connect(wifi_ssid, wifi_password)
                while wifi.radio.connected != True:
                    sleep(1)
                print('--- CONNECTED TO WIFI ---')
            except Exception as e:
                print('Exception when connecting to wifi: {}'.format(str(e)))
                pass
            break
        else:
            sleep(10 * count)
            count *= 2


def send_sensor_data(data):
    connect()
    response = session.post("{}/measurements".format(api_base_url), json=data)
    response.close()
    return response.status_code

def register_device(device_id):
    connect()
    response = session.post("{}/devices/".format(api_base_url), json={'id': device_id})
    response.close()
    return response.status_code