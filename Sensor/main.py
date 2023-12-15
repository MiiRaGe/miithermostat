import hih8000
import time

from microcontroller import cpu

import server_api
import onboard_led

from local_secrets import get_device_id, DEVICE_ID

def start_device(device_id):
    print('--- STARTING DEVICE_ID: {}'.format(device_id))
    print('--- REGISTERING DEVICE ---')
    server_api.register_device(device_id)
    while True:
        try:
            data = hih8000.measure()
            data["device_id"] = device_id
            onboard_led.blink()
            status_code = server_api.send_sensor_data(data)
            if status_code == 201:
                onboard_led.blink()
                onboard_led.set_error_led(False)
            else:
                print('--- Wrong status returned: {}'.format(status_code))
                onboard_led.set_error_led(True)
        except Exception as e:
            print('--- Error : {}'.format(str(e)))
            onboard_led.set_error_led(True)
            pass
        time.sleep(56) # Send sensor data every minute
    
if __name__ == "__main__":
    start_device(get_device_id())
